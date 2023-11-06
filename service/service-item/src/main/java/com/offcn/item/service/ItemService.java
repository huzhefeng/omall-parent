package com.offcn.item.service;

import java.util.Map;

public interface ItemService {

    //获取指定sku编号的sku数据
    Map<String, Object> getBySkuId(Long skuId);
}
