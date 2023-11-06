package com.offcn.mq.controller;

import com.offcn.mq.config.*;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendController {

    //注入操作rabbitMq的工具对象
    @Autowired
    private RabbitTemplate rabbitTemplate;


    //测试发送消息001
    @RequestMapping("send001")
    public String test001(){
        for (int i = 1; i <6 ; i++) {
            //参数1：接收消息队列名称 参数2：消息内容
            rabbitTemplate.convertAndSend(SimpleQueueConfig.simpleQueue,"测试消息:"+i);
        }
        return "send-ok";
    }

    //测试工作队列生产者
    @RequestMapping("send002")
    public String test002(){
        for (int i = 1; i <51 ; i++) {
            rabbitTemplate.convertAndSend(WorkQueueConfig.work,"测试消息:"+i);
        }
        return "send-ok";
    }

    //测试发送消息广播模式交换机
    @RequestMapping("send003")
    public String test003(){
        for (int i = 1; i <6 ; i++) {
            rabbitTemplate.convertAndSend(FanoutQueueConfig.fanoutExchange,"","测试消息:"+i);
        }

        return "send-ok";
    }

    //测试发送支持路由key消息到交换机
    //测试路由key 为 nan
    @RequestMapping("send004")
    public String test004(){
        for (int i = 1; i < 6; i++) {
            rabbitTemplate.convertAndSend(DirectQueueConfig.directExchange,"nan","测试消息:"+i);
        }
        return "send-ok";
    }

    //测试发送支持路由key消息到交换机
    //测试路由key 为 nv
    @RequestMapping("send005")
    public String test005(){
        for (int i = 1; i < 6; i++) {
            rabbitTemplate.convertAndSend(DirectQueueConfig.directExchange,"nv","测试消息:"+i);
        }
        return "send-ok";
    }

    //测试项交换机发送消息，指定路由key topic.test
    @RequestMapping("send006")
    public String test006(){
        for (int i = 1; i <6 ; i++) {
            rabbitTemplate.convertAndSend(TopicQueueConfig.topicExchange,"topic.test","测试消息,路由key等于topic.test："+i);
        }

        return "send-ok";
    }

    //测试项交换机发送消息，指定路由key topic.test.ok
    @RequestMapping("send007")
    public String test007(){
        for (int i = 1; i <6 ; i++) {
            rabbitTemplate.convertAndSend(TopicQueueConfig.topicExchange,"topic.test.ok","测试消息,路由key等于topic.test.ok："+i);
        }

        return "send-ok";
    }
}
