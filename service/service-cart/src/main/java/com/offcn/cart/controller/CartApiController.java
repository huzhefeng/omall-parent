package com.offcn.cart.controller;

import com.offcn.cart.service.CartService;
import com.offcn.common.result.Result;
import com.offcn.common.util.AuthContextHolder;
import com.offcn.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    //添加商品到购物车
    @PostMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum, HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        //判断userId是否为空
        if(StringUtils.isEmpty(userId)){
            //尝试获取临时用户id
            userId=  AuthContextHolder.getUserTempId(request);
        }
        //调用服务，添加商品到购物车
        cartService.addToCart(skuId,skuNum,userId);
        return Result.ok();
    }

    //读取购物车列表
    @GetMapping("cartList")
    public Result getCartList(HttpServletRequest request){
        //获取登录用户id
        String userId = AuthContextHolder.getUserId(request);
        //获取临时用户
        String userTempId = AuthContextHolder.getUserTempId(request);

        //调用服务，获取购物车数据
        List<CartInfo> cartList = cartService.getCartList(userId, userTempId);
        return Result.ok(cartList);
    }

    //更新状态方法
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable("skuId") Long skuId,@PathVariable("isChecked") Integer isCheckEd,HttpServletRequest request){

        //获取登录用户id
        String userId = AuthContextHolder.getUserId(request);

        //判断登录用户id是否为空
        if(StringUtils.isEmpty(userId)){
            //再次尝试获取临时用户
            userId=AuthContextHolder.getUserTempId(request);
        }

        //调用业务方法，执行修改状态
        cartService.checkCart(userId,isCheckEd,skuId);

        return Result.ok();
    }

    //删除方法
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,HttpServletRequest request){

        //获取登录用户id
        String userId = AuthContextHolder.getUserId(request);

        //判断登录用户id是否为空
        if(StringUtils.isEmpty(userId)){
            //尝试获取临时用户id
          userId=  AuthContextHolder.getUserTempId(request);
        }

        //调用服务，知识删除购物车商品
        cartService.deleteCart(userId,skuId);

        return Result.ok();
    }

    //读取指定用户，选中购物车集合数据集合
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") String userId){
        return cartService.getCartCheckedList(userId);
    }

    //读取数据库购物车数据写入到缓存
    @GetMapping("loadCartCache/{userId}")
    public Result loadCartCache(@PathVariable("userId") String userId){
        cartService.loadCartCache(userId);
        return Result.ok();
    }

}
