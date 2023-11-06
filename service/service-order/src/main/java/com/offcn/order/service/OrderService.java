package com.offcn.order.service;

import com.offcn.model.enums.ProcessStatus;
import com.offcn.model.order.OrderInfo;

public interface OrderService {

    //保存订单方法
    Long saveOrderInfo(OrderInfo orderInfo);

    //生成流水号（令牌，验证是否属于重复提交）
    String getTradeNo(String userId);

    //验证流水号
    boolean checkTradeCode(String userId,String tradeCodeNo);

    //删除流水号
    void deleteTradeNo(String userId);

    //验证库存
    boolean checkStock(Long skuId,Integer skuNum);

    //定义关闭订单方法
    void execExpiredOrder(Long orderId);

    /**
     * 根据订单Id 修改订单的状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);


    //根据指定订单编号，获取订单信息
    OrderInfo getOrderInfo(Long orderId);

}
