package com.offcn.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.model.user.UserAddress;
import com.offcn.user.mapper.UserAddressMapper;
import com.offcn.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<UserAddress>().eq("user_id", userId);
        return userAddressMapper.selectList(queryWrapper);
    }
}
