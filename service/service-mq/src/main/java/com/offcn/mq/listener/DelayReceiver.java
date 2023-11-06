package com.offcn.mq.listener;

import com.offcn.mq.config.DeadLetterMqConfig;
import com.offcn.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DelayReceiver {

    //定义监听死信队列
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void ReciveDeadLetter(String msg, Message message, Channel channel){
        System.out.println("接收到消息:"+msg);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("接收时间:"+dateFormat.format(new Date()));

        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
