package com.offcn.order.client;

import com.offcn.common.result.Result;
import com.offcn.model.order.OrderInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class OrderDegradeFeignClient implements OrderFeignClient{
    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }
}
