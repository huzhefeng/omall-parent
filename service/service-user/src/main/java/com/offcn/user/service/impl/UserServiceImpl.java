package com.offcn.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.model.user.UserInfo;
import com.offcn.user.mapper.UserInfoMapper;
import com.offcn.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl implements UserService {

    //注入用户数据操作接口
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Override
    public UserInfo login(UserInfo userInfo) {
        //首先我们获取用户输入的登录密码 是明文
        String passwd = userInfo.getPasswd();
        //我们要把明文密码转换成MD5加密密码
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes(StandardCharsets.UTF_8));

        //创建查询条件封装对象
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        //设置查询条件
        queryWrapper.eq("login_name",userInfo.getLoginName());
        //设置查询条件2 密码
        queryWrapper.eq("passwd",newPasswd);


        UserInfo userInfoDb = userInfoMapper.selectOne(queryWrapper);
        if(userInfoDb!=null){
            return userInfoDb;
        }
        return null;
    }
}
