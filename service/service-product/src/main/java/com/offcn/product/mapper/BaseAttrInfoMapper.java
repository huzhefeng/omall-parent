package com.offcn.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.model.product.BaseAttrInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

//定义根据指定一级、二级、三级分类编号，查询平台属性数据
    List<BaseAttrInfo> selectBaseAttrInfoList(@Param("category1Id") Long category1Id, @Param("category2Id")Long category2Id, @Param("category3Id")Long category3Id);


    //根据传入sku编号，获取对应平台属性信息
    List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(@Param("skuId") Long skuId);
}
