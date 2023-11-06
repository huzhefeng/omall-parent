package com.offcn.cart.client;

import com.offcn.common.result.Result;
import com.offcn.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CartDegradeFeignClient implements CartFeignClient{
    @Override
    public Result addToCart(Long skuId, Integer skuNum) {
        return null;
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        return null;
    }

    @Override
    public Result loadCartCache(String userId) {
        return null;
    }
}
