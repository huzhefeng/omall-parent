package com.offcn.all.controller;

import com.offcn.common.result.Result;
import com.offcn.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {

    //注入商品微服务调用接口
    @Autowired
    private ProductFeignClient productFeignClient;

    @GetMapping({"/","index.html"})
    public String index(HttpServletRequest request){
        Result result = productFeignClient.getBaseCategoryList();
       request.setAttribute("list",result.getData());
       //跳转到模板视图
        return  "index/index";
    }
}
