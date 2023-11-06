package com.offcn.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.offcn.order","com.offcn.common.config","com.offcn.common.service"})
@MapperScan(basePackages = {"com.offcn.order.mapper"})
@EnableFeignClients(basePackages = {"com.offcn.product.client","com.offcn.cart.client","com.offcn.user.client"})
public class ServiceOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class,args);
    }


}
