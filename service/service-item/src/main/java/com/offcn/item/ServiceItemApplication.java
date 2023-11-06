package com.offcn.item;

import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.offcn.item","com.offcn.product.client","com.offcn.list.client"})
@EnableFeignClients(basePackages = {"com.offcn.product.client","com.offcn.list.client"})
public class ServiceItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceItemApplication.class,args);
    }

    //声明openfeign日志级别
    @Bean
    public Logger.Level level(){
        return Logger.Level.FULL;
    }
}
