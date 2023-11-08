package com.offcn.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.model.enums.PaymentStatus;
import com.offcn.model.order.OrderInfo;
import com.offcn.model.payment.PaymentInfo;
import com.offcn.payment.mapper.PaymentInfoMapper;
import com.offcn.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    //注入支付表数据操作接口
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    /**
     * 保存支付记录
     *
     * @param orderInfo
     * @param paymentType 支付类型 1 微信支付  2支付宝支付 3网银支付
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {

        //为了保证保存支付记录处理方法幂等性
        //先查询当前数据库是否存在要添加支付记录
        //创建一个查询条件
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        //设置查询条件1：订单号
        queryWrapper.eq("order_id",orderInfo.getId());
        //设置一个查询条件2：支付类型
        queryWrapper.eq("payment_type",paymentType);

        //先去查询集合
        Integer count = paymentInfoMapper.selectCount(queryWrapper);
        //判断count>1表示支付记录已经存在，不在处理新增支付记录
        if(count>=1){
            return;
        }

        //没有该条支付记录，保存支付记录
        PaymentInfo paymentInfo = new PaymentInfo();
        //逐个设置属性
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        //设置支付金额
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        //设置交易内容
        paymentInfo.setSubject(orderInfo.getTradeBody());
        //设置交易状态 支付中
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        //设置创建时间
        paymentInfo.setCreateTime(new Date());

        //保存支付信息到数据库
        paymentInfoMapper.insert(paymentInfo);


    }

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo, String name) {
        //  select * from payment_info where out_trade_no = ? and payment_type = ?
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",name);
        return paymentInfoMapper.selectOne(paymentInfoQueryWrapper);
    }

    @Override
    public void paySuccess(String outTradeNo, String name, Map<String, String> paramMap) {
        //  update payment_info set trade_no=? ,payment_status = ? ,callback_time = ? where out_trade_no=? and payment_type = ?;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setTradeNo(paramMap.get("trade_no"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
        paymentInfo.setCallbackContent(paramMap.toString());

        //        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        //        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        //        paymentInfoQueryWrapper.eq("payment_type",name);
        // 第一个参数表示啥?要更新的内容  第二个参数表示啥？
        //  paymentInfoMapper.update(paymentInfo,paymentInfoQueryWrapper);
        this.updatePaymentInfo(outTradeNo,name,paymentInfo);
    }

    /**
     * 方法复用
     * @param outTradeNo
     * @param name
     * @param paymentInfo
     */
    public void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",name);
        // 第一个参数表示啥?要更新的内容  第二个参数表示啥？
        paymentInfoMapper.update(paymentInfo,paymentInfoQueryWrapper);
    }

}
