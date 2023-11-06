package com.offcn.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.offcn.common.result.Result;
import com.offcn.model.product.*;
import com.offcn.product.client.ProductFeignClient;
import com.offcn.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/product")
public class ProductApiController implements ProductFeignClient {

    @Autowired
    private ManageService manageService;


    //根据传入skuId读取skuInfo和配图数据
    @Override
    @GetMapping("inner/getSkuInfo/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        return Result.ok(skuInfo);
    }

    //根据传入三级分类编号，获取对应分类数据
    @Override
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id){
        BaseCategoryView categoryView = manageService.getCategoryViewByCategory3Id(category3Id);
        return categoryView;
    }

    //根据传入sku编号，获取商品价格
    @Override
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId){
        BigDecimal skuPrice = manageService.getSkuPrice(skuId);
        return skuPrice;
    }

    @Override
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId){
        return manageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @Override
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        return manageService.getSkuValueIdsMap(spuId);
    }

    //获取分类数据
    @Override
    @GetMapping("inner/getBaseCategoryList")
    public Result getBaseCategoryList(){
        List<JSONObject> baseCategoryList = manageService.getBaseCategoryList();
        return Result.ok(baseCategoryList);
    }

    //根据品牌编号，获取品牌信息
    @Override
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable("tmId") Long tmId){
        return manageService.getTrademarkByTmId(tmId);

    }

    //根据sku编号，获取平台属性数据集合
    @Override
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId){
      return   manageService.getAttrList(skuId);
    }
}
