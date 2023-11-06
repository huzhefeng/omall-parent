package com.offcn.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.offcn.model.enums.PaymentType;
import com.offcn.model.order.OrderInfo;
import com.offcn.order.client.OrderFeignClient;
import com.offcn.payment.config.AlipayConfig;
import com.offcn.payment.service.AlipayService;
import com.offcn.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayServiceImpl implements AlipayService {

    //注入连接到支付宝平台客户端对象
    @Autowired
    private AlipayClient alipayClient;

    //注入订单服务Feign接口
    @Autowired
    private OrderFeignClient orderFeignClient;

    //注入支付记录操作业务对象
    @Autowired
    private PaymentService paymentService;

    @Override
    public String createAliPay(Long orderId) throws AlipayApiException {
        //调用订单服务feign接口，根据订单编号，读取订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        //存储支付记录到数据库
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());

        //调用支付宝平台，获取支付信息
        //创建一个和支付宝平台进行交易请求的一个封装对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //设置同步通知回调地址
        request.setReturnUrl(AlipayConfig.return_payment_url);
        //设置异步通知回调地址
        request.setNotifyUrl(AlipayConfig.notify_payment_url);

        //创建Map集合封装其他业务参数
        Map<String, Object> map=new HashMap<>();
        //封装参数1：交易流水号 不能重复
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        //封装参数2：产品标识码
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        //封装参数3：支付金额 单位是元
        map.put("total_amount",orderInfo.getTotalAmount());
        //封装参数4：购买商品名称
        map.put("subject",orderInfo.getTradeBody());

        //设置请求参数到请求参数封装对象
        request.setBizContent(JSON.toJSONString(map));

        //调用支付宝客户端对象，发出支付请求

        return alipayClient.pageExecute(request).getBody();
    }
}
