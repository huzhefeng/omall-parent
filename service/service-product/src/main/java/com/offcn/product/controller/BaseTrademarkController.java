package com.offcn.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.result.Result;
import com.offcn.model.product.BaseTrademark;
import com.offcn.product.service.BaseTrademarkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "品牌管理接口")
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    //分页读取品牌列表数据方法
    @ApiOperation(value = "分页读取品牌列表数据方法")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",paramType = "path",dataType = "Long"),
            @ApiImplicitParam(name = "limit",value = "每页显示记录数",paramType = "path",dataType = "Long")
    })
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable("page") Long page,@PathVariable("limit") Long size){
        //创建一个分页参数封装对象
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, size);
        IPage<BaseTrademark> pageResult = baseTrademarkService.getPage(baseTrademarkPage);
        return Result.ok(pageResult);
    }

    //根据品牌编号，获取品牌详细信息
    @GetMapping("get/{id}")
    public Result get(@PathVariable("id") String id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    //新增品牌
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        try {
            baseTrademarkService.save(baseTrademark);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }

    //修改品牌数据
    @PutMapping("update")
    public Result updateById(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    //删除品牌数据
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable("id") Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    //加载全部品牌数据
    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> baseTrademarkList = baseTrademarkService.list(null);
        return Result.ok(baseTrademarkList);
    }
}
