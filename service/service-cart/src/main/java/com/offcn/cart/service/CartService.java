package com.offcn.cart.service;

import com.offcn.model.cart.CartInfo;

import java.util.List;

public interface CartService {

    //添加商品到购物车方法
    void addToCart(Long skuId,Integer skuNum,String userId);

    //通过指定userId获取该用户购物车集合数据
    //如果未登录使用临时用户userTempId
    List<CartInfo> getCartList(String userId,String userTempId);


    //修改指定sku商品的状态
    void checkCart(String userId,Integer isChecked,Long skuId);


    //业务删除指定用户、指定编号，购物车商品
    void deleteCart(String userId,Long skuId);


    //获取指定用户购买商品
    List<CartInfo> getCartCheckedList(String userId);

    //读取数据库购物车数据写入到缓存
    public List<CartInfo> loadCartCache(String userId);

}
