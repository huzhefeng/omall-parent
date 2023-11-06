package com.offcn.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.result.Result;
import com.offcn.model.product.BaseSaleAttr;
import com.offcn.model.product.SpuInfo;
import com.offcn.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;

    //定义分页查询spuinfo数据方法
    @GetMapping("{page}/{size}")
    public Result getSpuInfoPage(@PathVariable("page") Long page,@PathVariable("size") Long size, SpuInfo spuInfo){
        //创建一个分页参数封装对象，把页码、每页显示记录数封装到对象
        Page<SpuInfo> spuInfoPage = new Page<>(page, size);
        //调用服务，发出分页查询
        IPage<SpuInfo> iPage = manageService.getSpuInfoPage(spuInfoPage, spuInfo);
        return Result.ok(iPage);
    }

    //获取全部销售属性
    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    //保存spu方法
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        try {
            manageService.saveSpuInfo(spuInfo);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }
}
