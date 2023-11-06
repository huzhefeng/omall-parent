package com.offcn.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.offcn.model.product.BaseTrademark;

public interface BaseTrademarkService extends IService<BaseTrademark> {

    //分页读取品牌列表数据
    IPage<BaseTrademark> getPage(Page<BaseTrademark> pageParam);
}
