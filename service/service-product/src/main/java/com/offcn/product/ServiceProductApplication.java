package com.offcn.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(basePackages = {"com.offcn.product.mapper"})
@ComponentScan(basePackages = {"com.offcn.product","com.offcn.common.config","com.offcn.common.cache","com.offcn.common.config","com.offcn.common.service"})
@EnableSwagger2
@EnableCaching
public class ServiceProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class,args);
    }
}
