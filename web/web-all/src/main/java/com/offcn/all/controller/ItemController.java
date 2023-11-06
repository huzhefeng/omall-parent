package com.offcn.all.controller;

import com.offcn.common.result.Result;
import com.offcn.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;


    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable("skuId") Long skuId, Model model){
        //调用feign接口读取service-item服务的对应sku数据
        Result<Map> result = itemFeignClient.getItem(skuId);
        Map map = result.getData();
        //把读取到数据封装到model
        model.addAllAttributes(map);
        //跳转到模板
        return "item/index";
    }
}
