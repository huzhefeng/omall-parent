package com.offcn.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.result.Result;
import com.offcn.model.product.SkuInfo;
import com.offcn.model.product.SpuImage;
import com.offcn.model.product.SpuSaleAttr;
import com.offcn.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/product")
public class SkuManageController {

    @Autowired
    private ManageService manageService;

    //读取指定spu编号的商品配图
    @GetMapping("spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable("spuId") Long spuId){
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    //读取指定spu编号的销售属性、属性值数据
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);

        return Result.ok(spuSaleAttrList);
    }

    //保存sku数据处理方法
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    //读取skuinfo列表+分页
    @GetMapping("/list/{page}/{limit}")
    public Result index(@PathVariable("page") Long page,@PathVariable("limit") Long size){
        //把获取到分页参数，封装到一个分页参数封装对象
        Page<SkuInfo> skuInfoPage = new Page<>(page, size);
        IPage<SkuInfo> resultPage = manageService.getPage(skuInfoPage);
        return Result.ok(resultPage);
    }

    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){
        manageService.onSale(skuId);
        return Result.ok();
    }

    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){
        manageService.cancelSale(skuId);
        return Result.ok();
    }
}
