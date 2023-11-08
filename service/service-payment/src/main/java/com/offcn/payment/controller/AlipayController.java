package com.offcn.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.offcn.model.enums.PaymentStatus;
import com.offcn.model.enums.PaymentType;
import com.offcn.model.payment.PaymentInfo;
import com.offcn.payment.config.AlipayConfig;
import com.offcn.payment.service.AlipayService;
import com.offcn.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

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

    /**
     * 支付宝回调
     * @return
     */
    @RequestMapping("callback/return")
    public String callBack() {
        // 同步回调给用户展示信息
        return "redirect:" + AlipayConfig.return_order_url;
    }

    @Autowired
    private PaymentService paymentService;
    /**
     * 支付宝异步回调  必须使用内网穿透
     * @param paramMap
     * @param request
     * @return
     */
    @RequestMapping("callback/notify")
    @ResponseBody
    public String alipayNotify(@RequestParam Map<String, String> paramMap) {
        System.out.println("回来了！");

        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 交易状态
        String trade_status = paramMap.get("trade_status");
        String out_trade_no = paramMap.get("out_trade_no");
        // true
        if (signVerified) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
                // 但是，如果交易记录表中 PAID 或者 CLOSE  获取交易记录中的支付状态 通过outTradeNo来查询数据
                // select * from paymentInfo where out_trade_no=?
                PaymentInfo paymentInfo = paymentService.getPaymentInfo(out_trade_no, PaymentType.ALIPAY.name());
                if (paymentInfo.getPaymentStatus().equals(PaymentStatus.PAID.name()) || paymentInfo.getPaymentStatus().equals(PaymentStatus.ClOSED.name())){
                    return "failure";
                }
                // 正常的支付成功，我们应该更新交易记录状态
                paymentService.paySuccess(out_trade_no,PaymentType.ALIPAY.name(),paramMap);
                return "success";
            }

        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }

}
