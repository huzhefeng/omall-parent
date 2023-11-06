package com.offcn.list.client;

import com.offcn.common.result.Result;
import com.offcn.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

@FeignClient(value = "service-list",path = "api/list",fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    //更新浏览次数
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId);

    //上架调用接口
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId);

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId);

    //定义搜索方法
    @PostMapping
    public Result list(@RequestBody SearchParam searchParam) throws IOException;
}
