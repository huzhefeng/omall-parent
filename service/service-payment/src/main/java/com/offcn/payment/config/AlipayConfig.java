package com.offcn.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource("classpath:alipay.properties")
public class AlipayConfig {

    //定义读取网关地址
    @Value("${alipay_url}")
    private String alipay_url;

    @Value("${app_private_key}")
    private String app_private_key;

    @Value("${app_id}")
    private String app_id;

    public final static String format="json";
    public final static String charset="utf-8";
    public final static String sign_type="RSA2";

    public static String return_payment_url;

    public static  String notify_payment_url;

    public static  String return_order_url;

    public static  String alipay_public_key;

    @Value("${alipay_public_key}")
    public   void setAlipay_public_key(String alipay_public_key) {
        AlipayConfig.alipay_public_key = alipay_public_key;
    }

    @Value("${return_payment_url}")
    public   void setReturn_url(String return_payment_url) {
        AlipayConfig.return_payment_url = return_payment_url;
    }

    @Value("${notify_payment_url}")
    public   void setNotify_url(String notify_payment_url) {
        AlipayConfig.notify_payment_url = notify_payment_url;
    }

    @Value("${return_order_url}")
    public   void setReturn_order_url(String return_order_url) {
        AlipayConfig.return_order_url = return_order_url;
    }

    //声明一个连接到支付宝平台客户端对象
    @Bean
    public AlipayClient alipayClient(){
        return new DefaultAlipayClient(alipay_url,app_id,app_private_key,format,charset,alipay_public_key,sign_type);
    }
}
