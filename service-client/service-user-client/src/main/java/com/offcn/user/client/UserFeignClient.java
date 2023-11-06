package com.offcn.user.client;

import com.offcn.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "service-user",path = "/api/user",fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {

    @GetMapping("inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findAddressByUserId(@PathVariable("userId") String userId);
}
