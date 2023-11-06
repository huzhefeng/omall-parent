package com.offcn.user.client;

import com.offcn.model.user.UserAddress;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class UserDegradeFeignClient implements UserFeignClient{
    @Override
    public List<UserAddress> findAddressByUserId(String userId) {
        return null;
    }
}
