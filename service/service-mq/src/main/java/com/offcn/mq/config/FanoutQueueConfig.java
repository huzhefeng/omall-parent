package com.offcn.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FanoutQueueConfig {

    //声明两个队列名称
    public static final String fanout1 = "fanout_queue_1";

    public static final String fanout2 = "fanout_queue_2";


    //声明交换机名称
    public static final String fanoutExchange = "fanoutExchange";


    //声明队列1
    @Bean
    public Queue fanoutQueue1(){
        return new Queue(fanout1,true);
    }

    //声明队列2
    @Bean
    public Queue fanoutQueue2(){
        return new Queue(fanout2,true);
    }

    //声明广播模式交换机
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(fanoutExchange,true,true);
    }

    //绑定消息队列1到交换机
    @Bean
    public Binding bindingFanoutQueue1ToExchange(Queue fanoutQueue1,FanoutExchange fanoutExchange){
     return    BindingBuilder.bind(fanoutQueue1).to(fanoutExchange);
    }

    //绑定消息队列2到交换机
    @Bean
    public Binding bindingFanoutQueue2ToExchange(Queue fanoutQueue2,FanoutExchange fanoutExchange){
        return    BindingBuilder.bind(fanoutQueue2).to(fanoutExchange);
    }
}
