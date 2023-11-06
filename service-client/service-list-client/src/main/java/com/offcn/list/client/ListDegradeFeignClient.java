package com.offcn.list.client;

import com.offcn.common.result.Result;
import com.offcn.model.list.SearchParam;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ListDegradeFeignClient implements ListFeignClient{
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }

    /**
     * 下架商品
     *
     * @param skuId
     * @return
     */
    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }

    @Override
    public Result list(SearchParam searchParam) throws IOException {
        return Result.fail();
    }
}
