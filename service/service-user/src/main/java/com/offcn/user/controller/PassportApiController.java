package com.offcn.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.offcn.common.constant.RedisConst;
import com.offcn.common.result.Result;
import com.offcn.common.util.IpUtil;
import com.offcn.model.user.UserInfo;
import com.offcn.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    //注入redis操作工具对象
    @Autowired
    private RedisTemplate redisTemplate;



    //定义登录处理方法
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request){
//调用用户服务，登录方法
        UserInfo info = userService.login(userInfo);

        //判断登录返回info对象是否为空
        if(info!=null){
            //生成一个令牌
            String token= UUID.randomUUID().toString().replace("-","");

            //把令牌作为key存储到redis缓存
            //创建json对象
            JSONObject jsonObject = new JSONObject();
            //设置属性1：userId
            jsonObject.put("userId",info.getId());
            //设置属性2：登录这一刻 用户ip地址
            String ipAddress = IpUtil.getIpAddress(request);
            jsonObject.put("ip",ipAddress);

            //把令牌存储到redis缓存
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX+token, JSON.toJSONString(jsonObject),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

            //创建一个Map封装返回数据
            Map<String, Object> map=new HashMap<>();
            //封装token
            map.put("token",token);
            map.put("nickName",info.getNickName());

            //返回登录成功结果
            return Result.ok(map);
        }else {
            return Result.fail().message("登录失败");
        }
    }

    //退出登录
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        //把redis存储的token删除
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX+request.getHeader("token"));
        return Result.ok().message("退出登录成功");
    }
}
