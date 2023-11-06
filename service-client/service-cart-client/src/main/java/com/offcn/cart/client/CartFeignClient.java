package com.offcn.cart.client;

import com.offcn.common.result.Result;
import com.offcn.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(value = "service-cart",path = "api/cart",fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    @PostMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum);

    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") String userId);

    @GetMapping("loadCartCache/{userId}")
    public Result loadCartCache(@PathVariable("userId") String userId);
}
