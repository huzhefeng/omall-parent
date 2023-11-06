package com.offcn.payment.service;

import com.offcn.model.order.OrderInfo;

public interface PaymentService {



    /**
     * 保存支付记录
     * @param orderInfo
     * @param paymentType 支付类型 1 微信支付  2支付宝支付 3网银支付
     */
    void savePaymentInfo(OrderInfo orderInfo,String paymentType);
}
