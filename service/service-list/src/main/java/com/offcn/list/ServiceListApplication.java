package com.offcn.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.offcn.common","com.offcn.list"})
@EnableFeignClients(basePackages = {"com.offcn.product.client"})
@EnableElasticsearchRepositories(basePackages = {"com.offcn.list.repostiory"})
public class ServiceListApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceListApplication.class,args);
    }
}
