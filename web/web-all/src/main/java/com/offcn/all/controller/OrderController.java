package com.offcn.all.controller;

import com.offcn.common.result.Result;
import com.offcn.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class OrderController {

    //注入订单服务feign接口
    @Autowired
    private OrderFeignClient orderFeignClient;

    //跳转到订单确认页
    @RequestMapping("trade.html")
    public String trade(Model model){
        //调用订单服务feign接口，获取订单日确认页所需的数据
        Result<Map<String, Object>> result = orderFeignClient.trade();
        Map<String, Object> map = result.getData();
        //把获取到响应数据封装到model
        model.addAllAttributes(map);
        //跳转到订单确认页面
        return "order/trade";
    }



}
