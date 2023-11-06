package com.offcn.all.controller;

import com.offcn.cart.client.CartFeignClient;
import com.offcn.common.result.Result;
import com.offcn.model.product.SkuInfo;
import com.offcn.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {


    //把购物车feign接口注入
    @Autowired
    private CartFeignClient cartFeignClient;

    //注入sku商品feign接口
    @Autowired
    private ProductFeignClient productFeignClient;

    //查看购物车
    @RequestMapping("cart.html")
    public String index(HttpServletRequest request){
        //直接返回购物车列表模板
        return "cart/index";
    }

    //添加购物车
    @RequestMapping("addCart.html")
    public String addToCartList(Long skuId,Integer skuNum,HttpServletRequest request){

        //调用购物车服务feign接口，执行添加购物车操作
        cartFeignClient.addToCart(skuId,skuNum);


        //调用商品feign接口获取刚刚添加到购物车商品信息
        Result<SkuInfo> result = productFeignClient.getSkuInfo(skuId);
        SkuInfo skuInfo = result.getData();
        //把sku信息封装到request 用于在页面模板视图显示sku名称
        request.setAttribute("skuInfo",skuInfo);
        //把购买数量封装到request
        request.setAttribute("skuNum",skuNum);

        //跳转到添加购物车成功模板视图
        return "cart/addCart";

    }
}
