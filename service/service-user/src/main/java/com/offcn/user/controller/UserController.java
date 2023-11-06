package com.offcn.user.controller;

import com.offcn.model.user.UserAddress;
import com.offcn.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserAddressService userAddressService;

    //读取指定用户地址集合数据
    @GetMapping("inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findAddressByUserId(@PathVariable("userId") String userId){
        return userAddressService.findUserAddressListByUserId(userId);
    }
}
