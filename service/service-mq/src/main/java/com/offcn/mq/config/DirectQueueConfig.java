package com.offcn.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectQueueConfig {

    //声明两个队列名称
    public static final String direct1 = "direct_queue_1";

    public static final String direct2 = "direct_queue_2";

    //声明一个交换机名字
    public static final String directExchange = "directExchange";

    //声明队列1
    @Bean
    public Queue directQueue1(){
        return new Queue(direct1,true);
    }

    //声明队列2
    @Bean
    public Queue directQueue2(){
        return new Queue(direct2,true);
    }

    //声明支持路由key的交换机
    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(directExchange,true,true);
    }

    //把队列1绑定到交换机 指定路由key是 nv
    @Bean
    public Binding bindingDirectQueue1Exchange(Queue directQueue1,DirectExchange directExchange){
    return     BindingBuilder.bind(directQueue1).to(directExchange).with("nv");
    }

    //把队列2绑定到交换机 指定路由key是 nan
    @Bean
    public Binding bindingDirectQueue2Exchange(Queue directQueue2,DirectExchange directExchange){
        return     BindingBuilder.bind(directQueue2).to(directExchange).with("nan");
    }
}
