package com.offcn.product.controller;

import com.offcn.common.result.Result;
import com.offcn.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/product2/test")
public class TestController {

    @Autowired
    private TestService testService;

    //测试方法
    @GetMapping("testlock")
    public Result testAdd(){
        testService.testLock();
        return Result.ok();
    }

    //测试读
    @GetMapping("read")
    public Result testReadLock(){
        String s = testService.readLock();
        return Result.ok(s);
    }

    //测试写
    @GetMapping("write")
    public Result testWriteLock(){
        String s = testService.writeLock();
        return Result.ok(s);
    }

}
