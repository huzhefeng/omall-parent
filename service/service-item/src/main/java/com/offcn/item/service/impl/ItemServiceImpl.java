package com.offcn.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.common.result.Result;
import com.offcn.common.result.ResultCodeEnum;
import com.offcn.item.service.ItemService;
import com.offcn.list.client.ListFeignClient;
import com.offcn.model.product.BaseCategoryView;
import com.offcn.model.product.SkuInfo;
import com.offcn.model.product.SpuSaleAttr;
import com.offcn.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    //注入搜索服务feign接口
    @Autowired
    private ListFeignClient listFeignClient;

    //注入线程池对象
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

  /*  @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result=new HashMap<>();

        //调用feign接口，获取skuInfo信息

        Result<SkuInfo> skuInfoResult = productFeignClient.getSkuInfo(skuId);
        //获取返回结果
       SkuInfo skuInfo=skuInfoResult.getData();
       //把获取到结果封装到返回map
          result.put("skuInfo",skuInfo);
          //调用feign接口，读取销售属性数据
          List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
          //封装到map
          result.put("spuSaleAttrList",spuSaleAttrList);

          //调用feign接口，查询skuid对应属性id集合
          Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
         //把查询map转换成json字符串
          String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);

          //封装map
          result.put("valuesSkuJson",valuesSkuJson);

          //调用feign接口,获取商品最新价格
          BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
          //封装map
          result.put("price",skuPrice);

          //调用feign接口获取分类数据
       BaseCategoryView baseCategoryView= productFeignClient.getCategoryView(skuInfo.getCategory3Id());

       //封装到map
          result.put("categoryView",baseCategoryView);
        return result;
    }*/
    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result=new HashMap<>();

        //调用feign接口，获取skuInfo信息
    CompletableFuture<SkuInfo> skuInfoCompletableFuture=    CompletableFuture.supplyAsync(()->{
            Result<SkuInfo> skuInfoResult = productFeignClient.getSkuInfo(skuId);
            //获取返回结果
            SkuInfo skuInfo=skuInfoResult.getData();
            //保存返回结果到result
            result.put("skuInfo",skuInfo);
            return skuInfo;
        },threadPoolExecutor);

    //使用异步线程池更新商品浏览次数
        CompletableFuture<Void> incrHotScoreCompletableFuture  = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);

        //定义获取销售属性数据接口调用
       CompletableFuture<Void> spuSaleAttrCompletableFuture= skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            //调用feign接口，读取销售属性数据
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            //封装到map
            result.put("spuSaleAttrList",spuSaleAttrList);
        },threadPoolExecutor);


       //查询skuid对应属性id集合
        CompletableFuture<Void> skuValuesCompletableFuture=    skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //把查询map转换成json字符串
            String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);

            //封装map
            result.put("valuesSkuJson",valuesSkuJson);
        },threadPoolExecutor);

        //获取价格
    CompletableFuture<Void> priceCompletableFuture=   skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            //调用feign接口,获取商品最新价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            //封装map
            result.put("price",skuPrice);
        },threadPoolExecutor);

        //调用feign接口获取分类数据
   CompletableFuture<Void> baseCategoryCompletableFuture=    skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView baseCategoryView= productFeignClient.getCategoryView(skuInfo.getCategory3Id());

            //封装到map
            result.put("categoryView",baseCategoryView);
        },threadPoolExecutor);

      //把前面执行异步线程连接到一起，等待全部线程执行完毕
        CompletableFuture.allOf(skuInfoCompletableFuture,spuSaleAttrCompletableFuture,skuValuesCompletableFuture,priceCompletableFuture,baseCategoryCompletableFuture,incrHotScoreCompletableFuture).join();




        return result;
    }
}
