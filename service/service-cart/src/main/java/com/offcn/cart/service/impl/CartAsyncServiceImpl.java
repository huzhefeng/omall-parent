package com.offcn.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.cart.mapper.CartInfoMapper;
import com.offcn.cart.service.CartAsyncService;
import com.offcn.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class CartAsyncServiceImpl implements CartAsyncService {

    @Autowired
    private CartInfoMapper cartInfoMapper;


    @Async
    @Override
    public void updateCartInfo(CartInfo cartInfo) {
       //创建更新条件
        QueryWrapper<CartInfo> updateWrapper = new QueryWrapper<>();
        //设置条件1 userid
        updateWrapper.eq("user_id",cartInfo.getUserId());
        //设置条件2 skuid
        updateWrapper.eq("sku_id",cartInfo.getSkuId());
        //使用更新条件更新数据
        cartInfoMapper.update(cartInfo,updateWrapper);
    }

    @Async
    @Override
    public void saveCartInfo(CartInfo cartInfo) {

        cartInfoMapper.insert(cartInfo);
    }

    @Async
    @Override
    public void deleteCartInfo(String userId) {
        //创建删除条件：指定user_id
      cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id",userId));

    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        //创建修改条件
        QueryWrapper<CartInfo> updateWrapper = new QueryWrapper<>();
        //设置条件1，skuid
        updateWrapper.eq("sku_id",skuId);
        //设置条件2：userId
        updateWrapper.eq("user_id",userId);

        //创建一个对象，封装要修改数据
        CartInfo cartInfo = new CartInfo();
        //把要修改的 ：是否选中设置到封装对象
        cartInfo.setIsChecked(isChecked);

        //调用数据操作接口，按照条件，执行修改
        cartInfoMapper.update(cartInfo,updateWrapper);

    }

    @Override
    public void deleteCartInfo(String userId, Long skuId) {
        //创建删除条件
        QueryWrapper<CartInfo> deleteWrapper = new QueryWrapper<>();
        //设置删除条件1：userId
        deleteWrapper.eq("user_id",userId);
        //设置删除条件2：skuId
        deleteWrapper.eq("sku_id",skuId);

        //调用数据操作接口，执行删除
        cartInfoMapper.delete(deleteWrapper);
    }
}
