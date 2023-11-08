package com.offcn.all.controller;

import com.offcn.model.order.OrderInfo;
import com.offcn.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {

    //注入订单服务feign接口
    @Autowired
    private OrderFeignClient orderFeignClient;

    //获取指定订单编号订单信息、跳转到支付提示页
    @RequestMapping("pay.html")
    public String success(HttpServletRequest request, Model model){
        //调用订单服务feign接口获取指定订单号订单信息
        String orderId = request.getParameter("orderId");
        long longOrderId = Long.parseLong(orderId);
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(longOrderId);
        //把获取到订单对象，封装到model
        model.addAttribute("orderInfo",orderInfo);
        //跳转到支付提示页模板
        return "payment/pay";
    }
    /**
     * 支付成功页
     * @return
     */
    @GetMapping("pay/success.html")
    public String success() {
        return "payment/success";
    }

}
