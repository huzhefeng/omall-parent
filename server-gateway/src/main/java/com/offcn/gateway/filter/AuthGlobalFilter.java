package com.offcn.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.offcn.common.result.Result;
import com.offcn.common.result.ResultCodeEnum;
import com.offcn.common.util.IpUtil;
import org.apache.commons.io.output.TaggedOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthGlobalFilter implements GlobalFilter {

    //注入redis模板操作工具对象
    @Autowired
    private RedisTemplate redisTemplate;

    //声明一个对象，匹配路径工具对象
    private AntPathMatcher antPathMatcher=new AntPathMatcher();


    //读取配置文件中白名单数据
    @Value("${authUrls.url}")
    private String authUrls;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

       //首先获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取网关的请求路径 /qqq/test.html
       // URi:http://www.omall.com/qqq/test.html
        //getPath() /qqq/test.html
        String path = request.getURI().getPath();

        //判断请求路径是否属于内部接口，禁止访问
        if(antPathMatcher.match("/**/inner/**",path)){

         return    out(exchange.getResponse(),ResultCodeEnum.PERMISSION);
        }

        //不属于内部接口，继续访问

        //获取访问userId
        String userId = getUserId(request);

        //判断userId是否等于 -1 表示登录ip和验证ip不一致 非法登录
        if("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
            //返回一个拒绝访问
            return out(response,ResultCodeEnum.LOGING_FAIL);
        }

        //判断请求路径是否属于 需要认证路径
        if(antPathMatcher.match("/api/**/auth/**",path)){
            //判断userId是否为空
            if(StringUtils.isEmpty(userId)){
                ServerHttpResponse response = exchange.getResponse();
                //返回一个拒绝访问错误
                return out(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //验证请求路径是否属于白名单地址，也需要验证
        for (String authUrl : authUrls.split(",")) {

            //判断当前访问路径是否包含白名单的网页
            if(path.indexOf(authUrl)!=-1&&StringUtils.isEmpty(userId)){
                ServerHttpResponse response = exchange.getResponse();
               //设置响应状态码 303
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //设置响应头，指定要跳转到地址
                response.getHeaders().set("Location","http://passport.omall.com/login.html?originUrl="+request.getURI());
                return response.setComplete();
            }
        }

        //以上都是，放行
        //获取临时用户
        String userTempId = getUserTempId(request);

        //判断userId是否为空
        if(!StringUtils.isEmpty(userId)){
            //在放行同时UserId给他携带到对应目标服务
            request.mutate().header("userId",userId).build();
        }
        //判断userTempId是否为空
        if(!StringUtils.isEmpty(userTempId)){
            //把临时用户写到请求头
            request.mutate().header("userTempId",userTempId).build();
        }

        //携带userId、userTempId放行
        if(!StringUtils.isEmpty(userId)||!StringUtils.isEmpty(userTempId)) {
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);



    }

    //单独定义一个方法：获取临时用户userTempId
    private String getUserTempId(ServerHttpRequest request){
        //首先我们尝试从请求头获取临时用户
        String userTempId = request.getHeaders().getFirst("userTempId");
        //判断从请求头获取临时用户为空
        if(StringUtils.isEmpty(userTempId)){
            //再次尝试从cookie读取临时用户
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            //判断cookie是否为空
            if(cookie!=null){
                //从cookie读取临时用户值 ""
                userTempId=  cookie.getValue();
            }
        }

        //判断userTempId是否为空
        return StringUtils.isEmpty(userTempId)?null:userTempId;
    }

    //单独定义一个方法，获取UserId
    private String getUserId(ServerHttpRequest request){
        //首先，我们尝试从http协议，请求头，获取令牌
        String token = request.getHeaders().getFirst("token");

        //判断从请求头获取令牌是否为空
        if(StringUtils.isEmpty(token)){
            //如果从请求头获取token为空，再次尝试从cookie获取令牌
            HttpCookie cookie = request.getCookies().getFirst("token");
            //判断cookie是否为空
            if(cookie!=null){
                //从cookie对象读取令牌
                token=cookie.getValue();
            }
        }

        //如果token令牌不为空
        if(!StringUtils.isEmpty(token)){
            //使用token区redis缓存读取数据
         String jsonStr= (String) redisTemplate.opsForValue().get("user:login:"+token);
         //解析转换成JsonObject对象
            JSONObject jsonObject = JSON.parseObject(jsonStr, JSONObject.class);
            //获取 userId
            String userId = jsonObject.getString("userId");
            //获取ip
            String ip = jsonObject.getString("ip");
            //获取当前访问的用户ip

            String curiP = IpUtil.getGatwayIpAddress(request);
            //比对当前ip和redis缓存存储ip是否一致
            if(!ip.equals(curiP)){
                return "-1";
            }else {
                //把当前userId返回
                return userId;
            }
        }

        return "";
    }

    //单独定义一个进制访问输出
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum){
        //创建Result统一想要封装对象
        Result<Object> result = Result.build(null, resultCodeEnum);

        //把result相应结果封装对象，转换成json字符串
        String jsonStr = JSON.toJSONString(result);
        //把json字符串转换成一个字节数组，设置编码格式是UTF-8
        byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
        //把字节数组转换成数据缓冲区对象
        DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
        //设置想要浏览器头，设置想要类型是json格式
        response.getHeaders().add("Content-Type","application/json;charset=utf-8");
        //把数据缓冲区对象和response相应对象关联
        return response.writeWith(Mono.just(dataBuffer));
    }
}
