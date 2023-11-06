package com.offcn.list.reciver;

import com.offcn.common.consts.MqConst;
import com.offcn.list.service.SearchService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ListReceiver {


    //注入搜索服务
    @Autowired
    private SearchService searchService;

    //定义处理上架逻辑
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS,autoDelete = "false"),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperGoods(Long skuId, Message message, Channel channel){

        //判断skuid是否为空
        if(skuId!=null){
            //调用搜索服务执行上架
            searchService.upperGoods(skuId);
        }
        //手动确认收到消息
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
            //出现失败，拒绝确认
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //定义下架处理方法
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS,autoDelete = "false"),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerGoods(Long skuId, Message message, Channel channel){
        if(skuId!=null){
            //调用下架处理方法
            searchService.lowerGoods(skuId);
        }
        //手动确认收到消息
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
            //出现失败，拒绝确认
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
