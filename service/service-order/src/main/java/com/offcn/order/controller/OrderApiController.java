package com.offcn.order.controller;

import com.offcn.cart.client.CartFeignClient;
import com.offcn.common.result.Result;
import com.offcn.common.util.AuthContextHolder;
import com.offcn.model.cart.CartInfo;
import com.offcn.model.order.OrderDetail;
import com.offcn.model.order.OrderInfo;
import com.offcn.model.user.UserAddress;
import com.offcn.order.service.OrderService;
import com.offcn.product.client.ProductFeignClient;
import com.offcn.user.client.UserFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    //注入用户微服务feign接口
    @Autowired
    private UserFeignClient userFeignClient;

    //注入购物车服务feign接口
    @Autowired
    private CartFeignClient cartFeignClient;

    //注入订单服务
    @Autowired
    private OrderService orderService;

    //注入商品服务feign接口
    @Autowired
    private ProductFeignClient productFeignClient;

    //注入线程池对象
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    //获取订单结算页所需数据

    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request){
        //获取当前登录userId
        String userId = AuthContextHolder.getUserId(request);
        //调用用户服务feign接口，获取指定userId的地址集合
        List<UserAddress> userAddressList = userFeignClient.findAddressByUserId(userId);
        //调用购物车服务的feign接口，获取指定userId的选中的购物车数据集合
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);

        //创建一个订单明细集合
        List<OrderDetail> orderDetailList=new ArrayList<>();

        //定义一个变量记录总数量
        Integer countNum=0;
        //遍历购物车集合
        for (CartInfo cartInfo : cartInfoList) {
            //创建一个新的订单明细对象
            OrderDetail orderDetail = new OrderDetail();
            //设置订单明细属性1 sku_id
            orderDetail.setSkuId(cartInfo.getSkuId());
            //设置属性2：skuname
            orderDetail.setSkuName(cartInfo.getSkuName());
            //设置属性3：配图
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            //设置属性4：购买价格
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            //设置属性5：购买数量
            orderDetail.setSkuNum(cartInfo.getSkuNum());

            //累计总数量
            countNum+=cartInfo.getSkuNum();
            //把订单明细对象，加入到订单明细集合
            orderDetailList.add(orderDetail);
        }

        //创建订单对象
        OrderInfo orderInfo = new OrderInfo();
        //设置属性1：订单明细集合
        orderInfo.setOrderDetailList(orderDetailList);
        //设置属性2：合计金额
        orderInfo.sumTotalAmount();

        //获取交易流水号
        String tradeNo = orderService.getTradeNo(userId);


        //创建一个map封装返回数据
        Map<String, Object> map=new HashMap<>();
        //封装返回数据1：地址集合
        map.put("userAddressList",userAddressList);
        //封装返回数据2：封装订单明细集合
        map.put("detailArrayList",orderDetailList);
        //封装返回数据3：订单合计总金额
        map.put("totalAmount",orderInfo.getTotalAmount());
        //封装返回数据4：购买商品总数量
        map.put("totalNum",countNum);
        //生成交易流水号封装到返回map
        map.put("tradeNo",tradeNo);

        return Result.ok(map);

    }

    //该接口是否包装幂等性？
    //保存订单方法
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        //获取登录用户
        String userId = AuthContextHolder.getUserId(request);
        //设置登录用户id到订单对象
        orderInfo.setUserId(Long.parseLong(userId));

        //从request对象获取交易流水号
        String tradeNo = request.getParameter("tradeNo");
        //调用比较交易流水号方法
        boolean is = orderService.checkTradeCode(userId, tradeNo);


        if(!is){
            return Result.fail().message("交易流水号校验失败");
        }

        //一定要删除该交易流水号
        orderService.deleteTradeNo(userId);

        //获取订单详情集合
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        //创建集合，存储全部异步执行线程
        List<CompletableFuture> futureList=new ArrayList<>();
        //创建一个集合，存储执行失败结果
        List<String> errorList=new ArrayList<>();


        //遍历订单详情集合
        for (OrderDetail orderDetail : orderDetailList) {

            //创建验证库存异步执行线程
            CompletableFuture<Void> checkStockCompletableFuture  = CompletableFuture.runAsync(() -> {
                //逐个去验证库存 1S
                boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!result) {
                    // return Result.fail().message("商品编号为:"+orderDetail.getSkuId()+" 库存不足");
                    errorList.add("商品编号为:" + orderDetail.getSkuId() + " 库存不足");
                }
            }, threadPoolExecutor);
            //把检查库存异步线程加入线程集合
            futureList.add(checkStockCompletableFuture);

            //编写验证价格异步线程
            CompletableFuture<Void> checkPriceCompletableFuture  = CompletableFuture.runAsync(() -> {
                //验证价格
                //调用商品服务feign接口 2S
                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                //对比最新价格和当前购买详情价格
                if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                    //调用购物车，加载数据库数据到缓存方法
                    cartFeignClient.loadCartCache(userId);
                    //返回价格不同错误提示
                    // return Result.fail().message(orderDetail.getSkuName()+" 价格有变动");
                    errorList.add(orderDetail.getSkuName() + " 价格有变动");
                }
            }, threadPoolExecutor);
            futureList.add(checkPriceCompletableFuture);


        }

        //把全部线程，连接到一起，等全部线程执行完成
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();

        //判断错误集合是否为空
        if(errorList.size()>0){
            return Result.fail().message(StringUtils.join(errorList,","));//把集合全部错误消息使用，连接成一个大字符串
        }
        //调用订单服务，保存订单
        Long orderId = orderService.saveOrderInfo(orderInfo);


        return Result.ok(orderId);

    }

    //根据订单编号，获取订单主表+订单详情集合数据
    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable("orderId") Long orderId){

        return orderService.getOrderInfo(orderId);
    }
}
