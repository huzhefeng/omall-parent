package com.offcn.item.controller;

import com.offcn.common.result.Result;
import com.offcn.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    //根据指定sku编号，获取sku详情数据
    @GetMapping("/{skuId}")
    public Result getItem(@PathVariable("skuId") Long skuId){
        Map<String, Object> map = itemService.getBySkuId(skuId);
        return Result.ok(map);
    }
}
