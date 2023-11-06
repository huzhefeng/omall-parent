package com.offcn.order.client;

import com.offcn.common.result.Result;
import com.offcn.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.Map;

@FeignClient(value = "service-order",path = "api/order",fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade();

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable("orderId") Long orderId);
}
