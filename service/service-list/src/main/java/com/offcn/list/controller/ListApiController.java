package com.offcn.list.controller;

import com.offcn.common.result.Result;
import com.offcn.list.service.SearchService;
import com.offcn.model.list.Goods;
import com.offcn.model.list.SearchParam;
import com.offcn.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/list")
public class ListApiController {

    //注入ES模板操作工具对象
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    //注入搜索服务
    @Autowired
    private SearchService searchService;

    //建立索引+映射
    @GetMapping("/inner/createIndex")
    public Result createIndex(){
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    //上架调用接口
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId) {
        searchService.lowerGoods(skuId);
        return Result.ok();
    }

    //更新浏览次数
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId){
        searchService.incrHotScore(skuId);
        return Result.ok();
    }


    //定义搜索方法
    @PostMapping
    public Result list(@RequestBody SearchParam searchParam) throws IOException {
        SearchResponseVo searchResponseVo = searchService.search(searchParam);
        return Result.ok(searchResponseVo);
    }

}
