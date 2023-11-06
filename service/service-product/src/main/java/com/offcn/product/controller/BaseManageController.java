package com.offcn.product.controller;

import com.offcn.common.result.Result;
import com.offcn.model.product.BaseAttrInfo;
import com.offcn.model.product.BaseCategory1;
import com.offcn.model.product.BaseCategory2;
import com.offcn.model.product.BaseCategory3;
import com.offcn.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商品管理服务接口")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {

    //注入服务对象
    @Autowired
    private ManageService manageService;

    //获取全部一级分类数据
    @ApiOperation(value = "获取全部一级分类数据")
    @GetMapping("getCategory1")
    public  Result<List<BaseCategory1>> getCategory1(){
        List<BaseCategory1> category1List = manageService.getCategory1();
        //判断获取一级分类数据集合是否为空
        if(CollectionUtils.isEmpty(category1List)){
            return Result.fail();
        }else {
            return Result.ok(category1List);
        }
    }

    //根据指定一级分类编号，查询对应二级分类数据
    @ApiOperation(value = "根据指定一级分类编号，查询对应二级分类数据")
    @ApiImplicitParam(name = "category1Id",value = "一级分类编号",required = true,paramType = "path",dataType = "Long")
    @GetMapping("getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable("category1Id") Long category1Id){
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    //根据指定二级分类编号，查询对应三级分类数据
    @ApiOperation(value = "根据指定二级分类编号，查询对应三级分类数据")
    @ApiImplicitParam(name = "category2Id",value = "二级分类编号",required = true,paramType = "path",dataType = "Long")
    @GetMapping("getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable("category2Id") Long category2Id){
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);
        return Result.ok(category3List);
    }

    //获取指定一级分类、二级分类、三级分类 的平台属性数据集合
    @ApiOperation(value = "获取指定一级分类、二级分类、三级分类 的平台属性数据集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "category1Id", value = "一级分类编号", required = true, paramType = "path",dataType = "Long"),
            @ApiImplicitParam(name = "category2Id", value = "二级分类编号", required = true, paramType = "path",dataType = "Long"),
            @ApiImplicitParam(name = "category3Id", value = "三级分类编号", required = true, paramType = "path",dataType = "Long")
    })
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable("category1Id") Long category1Id,@PathVariable("category2Id")Long category2Id,@PathVariable("category3Id")Long category3Id){
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);
    }


    //平台属性新增操作
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        try {
            manageService.saveAttrInfo(baseAttrInfo);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }

    //根据id获取对应平台属性对象和平台属性值
    @GetMapping("getAttrValueList/{attrId}")
    public Result<BaseAttrInfo> getAttrValueList(@PathVariable("attrId") Long attrId){
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        return Result.ok(attrInfo);
    }

}
