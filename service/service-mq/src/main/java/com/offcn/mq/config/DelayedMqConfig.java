package com.offcn.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DelayedMqConfig {

    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";


    //声明队列
    @Bean
    public Queue delayQueue1(){
        return new Queue(queue_delay_1,true);
    }

    //声明交换机
    @Bean
    public CustomExchange customExchange(){
        //创建一个MAP设置交换机参数
        Map<String, Object> map=new HashMap<>();
        //设置交换机参数：支持路由模式交换机
        map.put("x-delayed-type","direct");
        return new CustomExchange(exchange_delay,"x-delayed-message",false,false,map);
    }

    //把交换机和队列进行绑定
    @Bean
    public Binding bindingDelayQueue1TocustomExchange(Queue delayQueue1,CustomExchange customExchange){
        return BindingBuilder.bind(delayQueue1).to(customExchange).with(routing_delay).noargs();
    }
}
