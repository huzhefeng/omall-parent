package com.offcn.payment.service;

import com.offcn.model.order.OrderInfo;
import com.offcn.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {



    /**
     * 保存支付记录
     * @param orderInfo
     * @param paymentType 支付类型 1 微信支付  2支付宝支付 3网银支付
     */
    void savePaymentInfo(OrderInfo orderInfo,String paymentType);
    //获取交易记录信息
    PaymentInfo getPaymentInfo(String out_trade_no, String name);

    //支付成功
    void paySuccess(String outTradeNo,String name, Map<String,String> paramMap);

    // 根据第三方交易编号，修改支付交易记录
    void updatePaymentInfo(String outTradeNo, String name
            , PaymentInfo paymentInfo);
}
