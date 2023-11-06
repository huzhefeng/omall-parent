package com.offcn.cart.service.impl;

import com.offcn.cart.service.HelloService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService {
    @Async//异步执行
    @Override
    public void testServiceMethod() {
        System.out.println("开始给执行---------");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("执行完毕----------");
    }
}
