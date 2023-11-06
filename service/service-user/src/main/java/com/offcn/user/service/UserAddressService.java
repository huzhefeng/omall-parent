package com.offcn.user.service;

import com.offcn.model.user.UserAddress;

import java.util.List;

public interface UserAddressService {

    //获取指定userId的地址列表
    List<UserAddress> findUserAddressListByUserId(String userId);
}
