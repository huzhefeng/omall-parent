package com.offcn.all;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.offcn.common.interceptor","com.offcn.all"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.offcn.item.client","com.offcn.product.client","com.offcn.list.client","com.offcn.cart.client","com.offcn.order.client"})
public class WebAllApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAllApplication.class,args);
    }
}
