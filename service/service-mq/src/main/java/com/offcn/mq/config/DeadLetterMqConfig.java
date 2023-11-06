package com.offcn.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DeadLetterMqConfig {

    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";

    //声明支持路由key的交换机
    @Bean
    public DirectExchange directSkillExchange(){
        return new DirectExchange(exchange_dead,false,false);
    }

    //声明正常消息队列
    @Bean
    public Queue queue1(){
        //创建map，设置队列绑定到私信交换机参数
        Map<String, Object> map=new HashMap<>();
        //封装参数1：绑定到死信交换机的名称
        map.put("x-dead-letter-exchange",exchange_dead);
        //封装参数2：指定路由key
        map.put("x-dead-letter-routing-key",routing_dead_2);
        //封装参数3：设置消息生命周期 单位是毫秒
        map.put("x-message-ttl",10*1000);
        //创建队列对象，设置参数
        //参数1：队列名称 参数2：是否持久化  参数3：是否独占 参数4：是否自定删除 参数5：设置参数
        return new Queue(queue_dead_1,true,false,false,map);
    }

    //声明第二个队列：存放死信消息队列
    @Bean
    public Queue queue2(){
        return new Queue(queue_dead_2,true);
    }

    //把死信交换机绑定到队列2
    @Bean
    public Binding bindingQueue2ToExchange(Queue queue2,DirectExchange directSkillExchange){
        return BindingBuilder.bind(queue2).to(directSkillExchange).with(routing_dead_2);
    }

    //把正常队列和死信交换机进行绑定
    @Bean
    public Binding bindingQueue1ToExchage(Queue queue1,DirectExchange directSkillExchange){
        return BindingBuilder.bind(queue1).to(directSkillExchange).with(routing_dead_1);
    }
}
