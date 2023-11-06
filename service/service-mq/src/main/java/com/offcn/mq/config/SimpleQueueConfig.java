package com.offcn.mq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleQueueConfig {

    //声明队列名称
    public final static String simpleQueue = "spring.test.queue";

    //声明一个消息队列的对象
    @Bean
    public Queue simpleQueue(){
        return new Queue(simpleQueue,true);//参数1：队列名称 参数2：是否需要持久化
    }
}
