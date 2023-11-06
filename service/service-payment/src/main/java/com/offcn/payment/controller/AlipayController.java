package com.offcn.payment.controller;

import com.alipay.api.AlipayApiException;
import com.offcn.payment.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    //发出支付请求
    @RequestMapping("submit/{orderId}")
    @ResponseBody
    public String submitOrder(@PathVariable("orderId") Long orderId){
        String body=null;
        try {
            //返回的是一页json代码
            body = alipayService.createAliPay(orderId);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return body;
    }
}
