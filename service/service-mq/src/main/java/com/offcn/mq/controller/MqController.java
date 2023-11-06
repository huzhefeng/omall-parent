package com.offcn.mq.controller;

import com.offcn.common.result.Result;
import com.offcn.common.service.RabbitService;
import com.offcn.mq.config.DeadLetterMqConfig;
import com.offcn.mq.config.DelayedMqConfig;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("mq")
public class MqController {

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //测试发送消息
    @GetMapping("send001")
    public Result sendMsg(){
        //创建一个日期时间格式化工具对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String msg = dateFormat.format(new Date());

        //使用rabbitService发送消息
        rabbitService.sendMsg("exchange.confirm","routing.confirm",msg);

        return Result.ok();
    }

    //测试发送延时消息
    @GetMapping("send002")
    public Result sendMsg2(){
        //创建一个日期时间格式化工具对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String msg = dateFormat.format(new Date());

        //发送消息
        rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead,DeadLetterMqConfig.routing_dead_1,"ok");

        System.out.println("消息发送完毕:"+msg);

        return Result.ok();
    }

    //测试发送延时消息2
    @GetMapping("send003")
    public Result sendMsg03(){
        //创建一个日期时间格式化工具对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String msg = dateFormat.format(new Date());


        //发送延时消息 指定延时时间
        rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(10*1000);//设置延时时间 单位毫秒
                return message;
            }
        });

        System.out.println("消息发送完毕:"+msg);
        return Result.ok();

    }
}
