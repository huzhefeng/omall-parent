package com.offcn.mq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkQueueConfig {

    public static final String work = "work_queue";//声明队列名称

    //创建工作队列对象
    @Bean
    public Queue workQueue(){
        return new Queue(work,true);
    }
}
