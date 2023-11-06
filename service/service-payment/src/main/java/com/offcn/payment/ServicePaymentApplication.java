package com.offcn.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.offcn.order.client"})
@ComponentScan(basePackages = {"com.offcn.payment","com.offcn.common"})
@MapperScan(basePackages = {"com.offcn.payment.mapper"})
public class ServicePaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicePaymentApplication.class,args);
    }
}
