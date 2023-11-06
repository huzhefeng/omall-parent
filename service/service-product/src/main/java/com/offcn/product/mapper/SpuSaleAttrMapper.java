package com.offcn.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.model.product.SpuSaleAttr;
import com.offcn.model.product.SpuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    //定义一个查询方法，根据指定spu编号，查询销售属性、销售属性值
    List<SpuSaleAttr> selectSpuSaleAttrList(@Param("spuId") Long spuId);

    //自定义查询：根据指定sku编号、spu编号查询对应销售属性数据
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId") Long spuId);
}
