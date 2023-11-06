package com.offcn.mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.offcn.mq","com.offcn.common.service","com.offcn.common.config"})
public class ServiceMqApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceMqApplication.class,args);
    }
}
