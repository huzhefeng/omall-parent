package com.offcn.cart.controller;

import com.offcn.cart.service.HelloService;
import com.offcn.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class HelloController {

    @Autowired
    private HelloService helloService;

    //定义一个同步测试调用
    @GetMapping("hello")
    public Result test1(){
        helloService.testServiceMethod();//调用异步方法，无需等待
        return Result.ok();
    }
}
