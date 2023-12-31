package com.offcn.item.client;

import com.offcn.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-item",path = "api/item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {
    @GetMapping("/{skuId}")
    public Result getItem(@PathVariable("skuId") Long skuId);
}
