package com.offcn.list.service;

import com.offcn.model.list.SearchParam;
import com.offcn.model.list.SearchResponseVo;

import java.io.IOException;

public interface SearchService {

    //上架商品（读取数据库商品数据写入到搜索引擎）
    void upperGoods(Long skuId);

    //下架商品（把搜索引擎数据删除）
    void lowerGoods(Long skuId);


    //记录浏览次数
    void incrHotScore(Long skuId);


    //搜索处理方法
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
