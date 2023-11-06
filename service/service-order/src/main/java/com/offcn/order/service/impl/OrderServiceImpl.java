package com.offcn.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.common.consts.MqConst;
import com.offcn.common.service.RabbitService;
import com.offcn.common.util.HttpClientUtil;
import com.offcn.model.enums.OrderStatus;
import com.offcn.model.enums.ProcessStatus;
import com.offcn.model.order.OrderDetail;
import com.offcn.model.order.OrderInfo;
import com.offcn.order.mapper.OrderDetailMapper;
import com.offcn.order.mapper.OrderInfoMapper;
import com.offcn.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    //注入消息发送服务
    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //声明一个变量读取配置文件中 库存系统地址和端口
    @Value("${ware.url}")
    private String WARE_URL;




    @Override
    public Long saveOrderInfo(OrderInfo orderInfo) {
        //计算订单合计金额
        orderInfo.sumTotalAmount();
        //设置订单状态 未付款
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //生成一个订单支付编号 offcn+当前系统时间毫秒值+生成1-1000随机数字
        String out_trade_no="OFFCN"+System.currentTimeMillis()+""+new Random().nextInt(1000);

        orderInfo.setOutTradeNo(out_trade_no);

        //订单创建时间：当前系统时间
        orderInfo.setCreateTime(new Date());
        //订单过期时间 往后推迟 1天
        Calendar calendar = Calendar.getInstance();
        //把天向后拨动 1天
        calendar.add(Calendar.DATE,1);
        //设置过期时间到订单对象
        orderInfo.setExpireTime(calendar.getTime());

        //设置进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());

        //获取订单明细集合
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        //创建一个可变字符串
        StringBuffer stringBuffer=new StringBuffer();
        //遍历订单明细
        for (OrderDetail orderDetail : orderDetailList) {
            //获取每个购买商品sku名称
           stringBuffer.append(orderDetail.getSkuName()+" ");
        }

        //判断拼接后订单备注是否长度大于100字符
        if(stringBuffer.length()>100){
            //截取前100字符
          orderInfo.setTradeBody(stringBuffer.toString().substring(0,100));
        }else {
            orderInfo.setTradeBody(stringBuffer.toString());
        }

        //调用订单数据操作接口，存储到数据库
        orderInfoMapper.insert(orderInfo);

        //遍历订单详情集合
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            //关联订单编号
            orderDetail.setOrderId(orderInfo.getId());
            //保存订单详情到数据库
            orderDetailMapper.insert(orderDetail);
        }


        //下单成功，发送延时消息
        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,orderInfo.getId(),MqConst.DELAY_TIME);


        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {

        //使用uuid随机生成一个交易流水号
       String uuid= UUID.randomUUID().toString().replace("-","");

       //获取存储到redis的缓存的key的名字
        String tradeNoKey = getTradeNoKey(userId);
        //存储到redis
        redisTemplate.opsForValue().set(tradeNoKey,uuid);

        return uuid;
    }

    //获取缓存中存储交易流水号的key
    private String getTradeNoKey(String userId){
        return "user:"+userId+":tradeCode";
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        //获取redis存储key名
        String tradeNoKey = getTradeNoKey(userId);
        //去redis读取指定key的值
        String redisUUid = (String) redisTemplate.opsForValue().get(tradeNoKey);
//比对前端传递过来流水号和redis存储流水号是否相同
        if(tradeCodeNo!=null&&redisUUid!=null&&tradeCodeNo.equals(redisUUid)){
            return true;
        }
        return false;
    }

    @Override
    public void deleteTradeNo(String userId) {
        String tradeNoKey = getTradeNoKey(userId);
        redisTemplate.delete(tradeNoKey);
    }

    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        String response = HttpClientUtil.doGet(WARE_URL + "/hasStock?skuId=" + skuId + "&num=" + skuNum);

        return "1".equals(response)?true:false;
    }

    @Override
    public void execExpiredOrder(Long orderId) {
        updateOrderStatus(orderId,ProcessStatus.CLOSED);
    }

    /**
     * 根据订单Id 修改订单的状态
     *
     * @param orderId
     * @param processStatus
     */
    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {

        //创建一个订单对象，封装要修改数据
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        //订单状态
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        //订单执行过程
        orderInfo.setProcessStatus(processStatus.name());
        orderInfoMapper.updateById(orderInfo);
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        //根据订单编号，读取订单主表数据
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //创建查询条件封装对象
        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        //设置查询条件：order_id
        queryWrapper.eq("order_id",orderId);
        //按照条件查询订单编号对应订单详情数据
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);
        //把订单详情集合数据关联设置到订单主表对象
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }
}
