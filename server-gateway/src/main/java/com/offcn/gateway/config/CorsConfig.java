package com.offcn.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;


import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    //定义网关解决跨域问题过滤拦截器
    @Bean
    public CorsWebFilter corsWebFilter(){
        //创建一个跨域配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //配置跨域属性1：允许跨域访问主机地址和端口
        corsConfiguration.addAllowedOrigin("*");
        //配置跨域属性2：是否允许获取客户端携带cookie
        corsConfiguration.setAllowCredentials(true);
        //配置跨域属性3：跨域请求允许请求方法 http请求方法 get、post、put
        corsConfiguration.addAllowedMethod("*");
        //允许跨域传递请求头名
        corsConfiguration.addAllowedHeader("*");
        //配置源地址对象
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
