package com.offcn.common.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class RabbitService {

    //注入rabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;


    //把发送消息封装一个方法
    public boolean sendMsg(String exchange,String routingKey,Object msg){
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,new CorrelationData(UUID.randomUUID().toString()));
        return true;
    }

    //封装一个发送延时消息方法
    public boolean sendDelayMessage(String exchange,String routingKey,Object msg,int delayTime){
        //生成消息唯一标识
        String uuid = UUID.randomUUID().toString();
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
               message.getMessageProperties().setDelay(delayTime*1000);
                return message;
            }
        });
        return true;
    }
}
