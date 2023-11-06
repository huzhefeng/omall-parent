package com.offcn.product.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManageService {

    /**
     * 获取全部一级分类数据方法
     */
    List<BaseCategory1> getCategory1();

    /**
     *  根据指定一级分类编号，获取对应所属二级分类数据
     * @param category1Id
     * @return
     */

    List<BaseCategory2> getCategory2(Long category1Id);



    /**
     * 根据指定二级分类编号，获取对应所属三级分类数据
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 获取平台属性列表
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id,Long category2Id,Long category3Id);


    //保存平台属性到数据库
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    //根据平台属性编号，读取对应平台属性值
    BaseAttrInfo getAttrInfo(Long attrId);



    /**
     * 根据查询条件返回spuInfo列表数据+分页
     * @param pageParam  分页查询参数
     * @param spuInfo    查询条件
     * @return
     */
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam,SpuInfo spuInfo);


    //获取全部销售属性数据集合
    List<BaseSaleAttr> getBaseSaleAttrList();


    //保存spu数据方法
    void saveSpuInfo(SpuInfo spuInfo);


    //根据指定spu商品编号，读取对应spu商品配图
    List<SpuImage> getSpuImageList(Long spuId);

    //获取指定spu编号的，对应销售属性
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);


    //保存sku处理方法
    void saveSkuInfo(SkuInfo skuInfo);

    //获取sku列表+支持分页
    IPage<SkuInfo> getPage(Page<SkuInfo> pageParam);


    //sku商品上架操作方法
    void onSale(Long skuId);

    //SKU商品下架操作方法
    void cancelSale(Long skuId);


    //根据sku编号，查询sku数据
    SkuInfo getSkuInfo(Long skuId);

    //根据指定三级分类编号，获取分类信息（一级、二级、三级）
    BaseCategoryView getCategoryViewByCategory3Id(Long ctaegory3Id);

    //根据指定sku编号，获取商品价格
    BigDecimal getSkuPrice(Long skuId);


    //根据spu编号、sku编号，获取对应销售属性值
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId,Long skuId);


    //根据spuId 查询map 集合属性
    Map getSkuValueIdsMap(Long spuId);

    //获取全部分类数据、按照层级关系逐层封装json
    List<JSONObject> getBaseCategoryList();


    //根据品牌id，获取品牌信息
    BaseTrademark getTrademarkByTmId(Long tmId);


    //根据传入sku编号，获取对应平台属性集合数据
    List<BaseAttrInfo> getAttrList(Long skuId);
}
