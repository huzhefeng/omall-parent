package com.offcn.order.reciver;

import com.offcn.common.consts.MqConst;
import com.offcn.model.enums.OrderStatus;
import com.offcn.model.order.OrderInfo;
import com.offcn.order.mapper.OrderInfoMapper;
import com.offcn.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderReceiver {

    //注入订单表数据操作接口
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderService orderService;

    //定义监听取消订单延时队列消息
    @RabbitListener(queues = {MqConst.QUEUE_ORDER_CANCEL})
    public void reciveCancalOrderMessage(Long orderId, Message message, Channel channel){
        try {
            //判断订单编号是否为空
            if(orderId!=null){
                //根据订单编号，去数据库查询订单信息
                OrderInfo orderInfo = orderInfoMapper.selectById(orderId);

                //判断订单对象是否为空，订单状态是否是未支付
                if(orderInfo!=null&&orderInfo.getOrderStatus().equals("UNPAID")){
                    //关闭订单
                    orderService.execExpiredOrder(orderId);

                }
            }

            //手动确认消息接收
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            //确认不接受
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
