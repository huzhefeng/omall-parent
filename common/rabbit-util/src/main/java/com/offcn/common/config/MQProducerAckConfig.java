package com.offcn.common.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ReturnCallback,RabbitTemplate.ConfirmCallback {

    //注入RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

   //初始化执行方法
    @PostConstruct//在对象初始化调用指定方法
    public void init(){
        //把重新的RabbitTemplate两个方法设置到当前RabbitTemplate
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 消息到达交换机的状态
     * @param correlationData 消息信息-标识符
     * @param ack  是否确认
     * @param cause 失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        if(ack){
            log.info("消息发送成功:"+ JSON.toJSONString(correlationData));
        }else {
            log.info("消息发送失败:"+cause+ "详细数据:"+JSON.toJSONString(correlationData));
        }

    }

    /**
     * 消息从交换机转发到队列时失败调用本方法
     * @param message 消息内容
     * @param replyCode 响应码
     * @param replyText 响应内容
     * @param exchange 交换机名
     * @param routingKey 路由key
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息从交换机转发到队列时失败");
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);
    }
}
