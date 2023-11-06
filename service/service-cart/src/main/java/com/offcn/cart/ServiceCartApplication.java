package com.offcn.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.offcn.common.config","com.offcn.cart"})
@MapperScan(basePackages = {"com.offcn.cart.mapper"})
@EnableFeignClients(basePackages = {"com.offcn.product.client"})
@EnableAsync//开启异步
public class ServiceCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCartApplication.class,args);
    }
}
