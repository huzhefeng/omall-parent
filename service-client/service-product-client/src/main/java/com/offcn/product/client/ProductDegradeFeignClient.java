package com.offcn.product.client;

import com.offcn.common.result.Result;
import com.offcn.model.product.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@Component
public class ProductDegradeFeignClient implements ProductFeignClient{
    /**
     * 根据skuId获取sku信息
     *
     * @param skuId
     * @return
     */
    @Override
    public Result<SkuInfo> getSkuInfo(Long skuId) {
        return null;
    }

    /**
     * 通过三级分类id查询分类信息
     *
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    /**
     * 获取sku最新价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return null;
    }

    /**
     * 根据spuId，skuId 查询销售属性集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return null;
    }

    /**
     * 根据spuId 查询map 集合属性
     *
     * @param spuId
     * @return
     */
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        return null;
    }

    @Override
    public Result getBaseCategoryList() {
        return null;
    }

    @Override
    public BaseTrademark getTrademark(Long tmId) {
        return null;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return null;
    }
}
