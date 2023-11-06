package com.offcn.payment.service;

import com.alipay.api.AlipayApiException;

public interface AlipayService {

    //发起支付业务方法
    String createAliPay(Long orderId) throws AlipayApiException;
}
