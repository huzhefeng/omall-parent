package com.offcn.cart.service;

import com.offcn.model.cart.CartInfo;

public interface CartAsyncService {

    //更新购物车数据
    void updateCartInfo(CartInfo cartInfo);

    //保存购物车数据方法
    void saveCartInfo(CartInfo cartInfo);

    //删除指定用户全部购物车数据库数据
    void deleteCartInfo(String userId);

    //修改指定购物车购买商品的选中状态
    void checkCart(String userId,Integer isChecked,Long skuId);


    //删除指定用户、指定skuid的购买的商品
    void deleteCartInfo(String userId,Long skuId);

}
