package com.offcn.user.service;

import com.offcn.model.user.UserInfo;

public interface UserService {

    //定义登录处理方法
    UserInfo login(UserInfo userInfo);
}
