package com.offcn.mq.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConfirmReceiver {

    //定义一个接收消息方法
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm",durable = "true",autoDelete = "false"),
            key = {"routing.confirm"}
    ))
    public void process(Message message, Channel channel){
        System.out.println("接收到消息:"+new String(message.getBody()));
        System.out.println("消息编号:"+message.getMessageProperties().getDeliveryTag());

        //去手动确认
        try {
           // channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        } catch (IOException e) {
            e.printStackTrace();
            //确认不接收
            //参数1：消息编号
            //参数2：是否批量操作
            //参数3：不确认 采用什么处理方式 false 消息丢弃 true 会把消息重新放回消息队列
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
