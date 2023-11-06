package com.offcn.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.model.product.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    List<Map> selectSaleAttrValuesBySpu(Long spuId);
}
