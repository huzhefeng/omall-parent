package com.offcn.mq.listener;

import com.offcn.mq.config.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveListener {

    //定义一个接收消息方法001
    @RabbitListener(queues = SimpleQueueConfig.simpleQueue)
    public void reciveMsg001(String msg){
        System.out.println("接收到消息:"+msg);
    }


    //定义监听工作队列消费者1
    @RabbitListener(queues = WorkQueueConfig.work)
    public void resviveMsg002(String msg){
        System.out.println("消费者1，监听到消息:"+msg);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //定义监听工作队列消费者2
    @RabbitListener(queues = WorkQueueConfig.work)
    public void resviveMsg003(String msg){
        System.out.println("消费者2，监听到消息:"+msg);
    }


    //监听消息队列1消息
    @RabbitListener(queues = FanoutQueueConfig.fanout1)
    public void reciveMsg004(String msg){
        System.out.println("消费者1，监听到消息:"+msg);
    }

    //监听消息队列2消息
    @RabbitListener(queues = FanoutQueueConfig.fanout2)
    public void reciveMsg005(String msg){
        System.out.println("消费者2，监听到消息:"+msg);
    }

    //消费者1
    @RabbitListener(queues = DirectQueueConfig.direct1)
    public void reciveMsg006(String msg){
        System.out.println("消费者1，接收到消息:"+msg);
    }

    //消费者2
    @RabbitListener(queues = DirectQueueConfig.direct2)
    public void reciveMsg007(String msg){
        System.out.println("消费者2，接收到消息:"+msg);
    }

    //消费者1
    @RabbitListener(queues = TopicQueueConfig.topic1)
    public void reciveMsg008(String msg){
        System.out.println("消费者1，接收到消息:"+msg);
    }
    //消费者2
    @RabbitListener(queues = TopicQueueConfig.topic2)
    public void reciveMsg009(String msg){
        System.out.println("消费者2，接收到消息:"+msg);
    }
}
