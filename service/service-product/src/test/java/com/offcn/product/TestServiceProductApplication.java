package com.offcn.product;

import com.offcn.model.product.*;
import com.offcn.product.mapper.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestServiceProductApplication {

    //注入当前测试springboot环境里面一级分类数据操作接口
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;


    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisTemplate redisTemplate;



    //测试一级分类查询
    @Test
    public void testQueryBaseCategory1FindAll(){
        List<BaseCategory1> baseCategory1List = baseCategory1Mapper.selectList(null);
        for (BaseCategory1 baseCategory1 : baseCategory1List) {
            System.out.println("一级分类名称:"+baseCategory1.getName()+" id:"+baseCategory1.getId());
        }
    }

    //测试获取指定一级、二级、三级分类 平台属性+属性值数据
    @Test
    public void test2(){
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectBaseAttrInfoList(0L, 0L, 61L);
        for (BaseAttrInfo baseAttrInfo : baseAttrInfoList) {
            System.out.println("name:"+baseAttrInfo.getAttrName()+" id:"+baseAttrInfo.getId());
            //获取平台属性值list
            for (BaseAttrValue baseAttrValue : baseAttrInfo.getAttrValueList()) {
                System.out.println("name:"+baseAttrValue.getValueName());
            }
        }
    }

    //测试获取指定spu、sku编号数据
    @Test
    public void test3(){
        List<SpuSaleAttr> spuSaleAttrValueList = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(51L, 35L);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrValueList) {
            System.out.println("id:"+spuSaleAttr.getBaseSaleAttrId()+" name:"+spuSaleAttr.getSaleAttrName());
        }
    }

    //测试获取指定spu编号
    @Test
    public void test4(){
        List<Map> maps = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(35L);
        for (Map map : maps) {
            System.out.println("id:"+map.get("sku_id")+" spuvalue:"+map.get("value_ids"));
        }
    }

    //测试存储一个数据到redis
    @Test
    public void testRedis1(){
       /* redisTemplate.boundValueOps("aaa").set("123456yy");
        String s="bbb";
        Map<String,String> map=new HashMap<>();
        map.put("id","122");
        map.put("name","你好");
        map.put("age","18");
        redisTemplate.boundValueOps(s).set(map);*/
        String key1="sku:1314:info";
        redisTemplate.boundValueOps(key1).set("qqqqqqq");

        String key2="sku:1315:info";
        redisTemplate.boundValueOps(key2).set("wwwwwwww");
    }
}
