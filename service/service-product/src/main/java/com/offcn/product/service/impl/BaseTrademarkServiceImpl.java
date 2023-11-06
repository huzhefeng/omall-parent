package com.offcn.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.model.product.BaseTrademark;
import com.offcn.product.mapper.BaseTrademarkMapper;
import com.offcn.product.service.BaseTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper,BaseTrademark> implements BaseTrademarkService {

    //注入品牌数据操作接口
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    @Override
    public IPage<BaseTrademark> getPage(Page<BaseTrademark> pageParam) {
       //创建一个查询条件构建器对象
        QueryWrapper<BaseTrademark> queryWrapper = new QueryWrapper<>();
        //按照id编号，进行排序
        queryWrapper.orderByAsc("id");

        return baseTrademarkMapper.selectPage(pageParam,queryWrapper);
    }
}
