package com.offcn.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.cache.OmallCache;
import com.offcn.common.constant.RedisConst;
import com.offcn.common.consts.MqConst;
import com.offcn.common.service.RabbitService;
import com.offcn.model.product.*;
import com.offcn.product.mapper.*;
import com.offcn.product.service.ManageService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    //注入平台属性值数据操作接口
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    //注入spuInfo数据操作接口
    @Autowired
    private SpuInfoMapper spuInfoMapper;


    //注入消息发送工具对象
    @Autowired
    private RabbitService rabbitService;

    //注入销售属性数据操作接口
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    //注入分类视图数据操作接口
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    //注入redis操作工具对象
    @Autowired
    private RedisTemplate redisTemplate; // aaa

    //注入redisson客户端工具对象
    @Autowired
    private RedissonClient redissonClient;

    //注入品牌的数据操作接口
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;
   // StringRedisTemplate 处理字符串   aaa
    /**
     * 获取全部一级分类数据方法
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 根据指定一级分类编号，获取对应所属二级分类数据
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        //创建查询二级分类数据查询条件构建器对象
        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        //设置查询条件 对应一级分编号
        queryWrapper.eq("category1_id",category1Id);
        return baseCategory2Mapper.selectList(queryWrapper);
    }

    /**
     * 根据指定二级分类编号，获取对应所属三级分类数据
     *
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category2_id",category2Id);

        return baseCategory3Mapper.selectList(queryWrapper);
    }

    /**
     * 获取平台属性列表
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Transactional(rollbackFor = Exception.class)//增加事务注解
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断平台属性数据是修改?新增
        if(baseAttrInfo.getId()!=null){
            //要做修改操作
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else {
            //表示新增
            baseAttrInfoMapper.insert(baseAttrInfo);
        }


        //创建删除条件，把平台属性对应属性值删除
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        //设置删除条件：平台属性编号
        queryWrapper.eq("attr_id",baseAttrInfo.getId());
        //执行删除平台属性值
        baseAttrValueMapper.delete(queryWrapper);
        //批量新增平台属性值
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        //判断平台属性值集合是否为空
        if(!CollectionUtils.isEmpty(baseAttrValueList)){
            //循环遍历平台属性值集合
            for (BaseAttrValue baseAttrValue : baseAttrValueList) {
                //关联平台属性值和平台属性编号
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                //逐个保存平台属性值到数据库
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        //根据平台属性id读取对应平台属性数据
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);

        //调用获取指定平台属性id对应平台属性值集合
        List<BaseAttrValue> attrValueList = getAttrValueList(attrId);
        //关联设置平台属性值集合到平台属性对象
        baseAttrInfo.setAttrValueList(attrValueList);
        return baseAttrInfo;
    }

    //单独定义一个方法，获取对应平台属性编号的平台属性值集合
    private List<BaseAttrValue> getAttrValueList(Long attrId){
        //创建一个查询条件
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_id",attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(queryWrapper);
        return baseAttrValueList;
    }

    /**
     * 根据查询条件返回spuInfo列表数据+分页
     *
     * @param pageParam 分页查询参数
     * @param spuInfo   查询条件
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        //创建查询条件构建器对象
        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        //设置查询条件
        queryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        //设置排序条件
        queryWrapper.orderByDesc("id");
        //按照指定查询条件，发出分页查询
        return spuInfoMapper.selectPage(pageParam,queryWrapper);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {

        //保存spuInfo表
        spuInfoMapper.insert(spuInfo);
        //spuImage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //判断商品配图集合是否为空
        if(!CollectionUtils.isEmpty(spuImageList)){
            //遍历配图集合
            for (SpuImage spuImage : spuImageList) {
                //设置关联到spuInfo表
                spuImage.setSpuId(spuInfo.getId());
                //保存配图到数据库
                spuImageMapper.insert(spuImage);
            }
        }
        //处理销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //判断销售属性集合是否为空
        if(!CollectionUtils.isEmpty(spuSaleAttrList)){
            //遍历销售属性集合
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                //设置关联到spuInfo表
                spuSaleAttr.setSpuId(spuInfo.getId());
                //保存销售属性
                spuSaleAttrMapper.insert(spuSaleAttr);

                //获取对应销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                //判断销售属性值集合是否为空
                if(!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                    //遍历销售属性值集合
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        //设置关联到spuInfo
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        //设置关联销售属性名称
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        //保存销售属性值到数据库
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }


    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        //创建查询条件封装对象
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        //设置查询条件：spu编号
        queryWrapper.eq("spu_id",spuId);
        return spuImageMapper.selectList(queryWrapper);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //保存skuInfo数据到数据库
        skuInfoMapper.insert(skuInfo);
        //获取sku配图集合
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        //判断sku配图集合是否为空
        if(!CollectionUtils.isEmpty(skuImageList)){
            //遍历集合
            for (SkuImage skuImage : skuImageList) {
                //关联sku编号
                skuImage.setSkuId(skuInfo.getId());
                //保存sku配图到数据库
                skuImageMapper.insert(skuImage);
            }
        }

        //获取商品销售属性集合
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //判断集合是否为空
        if(!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            //遍历集合
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //关联sku编号
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                //关联spu编号
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                //保存商品销售属性到数据库
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        //获取商品平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //判断平台属性集合是否为空
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            //变量平台属性集合
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                //设置关联sku编号
                skuAttrValue.setSkuId(skuInfo.getId());
                //保存平台属性到数据库
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

    }

    @Override
    public IPage<SkuInfo> getPage(Page<SkuInfo> pageParam) {
        //创建一个查询条件
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        //设置排序条件：id desc
        queryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(pageParam,queryWrapper);
    }

    @Override
    public void onSale(Long skuId) {
        //思路1：根据skuId,去数据库查询skuInfo数据、把查询到结果对象修改状态 1
           // 更新保存skuInfo对象数据到数据库

        //思路2：创建修改条件
        SkuInfo skuInfo = new SkuInfo();
        //设置修改条件
        skuInfo.setId(skuId);
        //设置上架
        skuInfo.setIsSale(1);
        //直接更新skuInfo对象，按照创建skuInFO对象作为修改条件
        skuInfoMapper.updateById(skuInfo);

        //调用消息工具对象，发送消息
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_UPPER,skuId);


    }

    @Override
    public void cancelSale(Long skuId) {

        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        //当下架发送消息到交换机
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_LOWER,skuId);
    }

   // @Cacheable(cacheNames = "skuInfo",key = "#skuId")
    @OmallCache(prefix = RedisConst.SKUKEY_PREFIX)
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
      //调用使用redis存储数据
       // return getSkuInfoRedis(skuId);
      //  return getSkuInfoRedisson(skuId);
        return getSkuInfoDB(skuId);
    }

    /**
     * 从数据库读取skuInfo数据+配图数据
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoDB(Long skuId) {
        //根据sku编号，去数据库读取skuInfo数据
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo!=null) {
            //获取指定sku编号锁对应配图
            QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
            //设置查询条件：sku_id
            queryWrapper.eq("sku_id", skuId);
            List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);
            //把获取到配图集合关联设置到skuInfo对象
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    @OmallCache(prefix = "BaseCategoryView")
    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        //根据sku编号，去数据库读取最新sku数据
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //判断skuInfo是否为空
        if(skuInfo!=null){
            return skuInfo.getPrice();
        }else {
            return new BigDecimal(0);
        }

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId, Long skuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> result=new HashMap<>();

        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        //判断查询结果集合是否为空
        if(mapList!=null&&mapList.size()>0){
            //循环遍历集合
            for (Map map : mapList) {
             //把查询到结果封装map
                result.put(map.get("value_ids"),map.get("sku_id"));
            }
        }
        return result;
    }

    //使用redis实现分布式锁，读取skuInfo数据
    private SkuInfo getSkuInfoRedis(Long skuId){

        //定义一个返回SkuInfo对象
        SkuInfo skuInfo=null;

        try {
            //定义在缓存中存储skuinfo数据的key
            String skuKey= RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;

            //尝试去redis缓存读取skuInfo数据
            skuInfo= (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            //判断从redis缓存读取数据是否为空
            if(skuInfo==null){
                //从缓存读取数据失败
                //定义一个锁的key
                String lockKey=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                //生成一个随机数 qqqqq-111ee-33ee-33eeede
                String uuid = UUID.randomUUID().toString().replace("-", "");
                //尝试去redis获取锁
                Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断如果获取锁成功
                if(isLock){
                    System.out.println("获取到分布式锁");
                    skuInfo= getSkuInfoDB(skuId);
                    //继续判断skuInfo是否为空
                    if(skuInfo==null){
                        //创建一个空skuInfo对象
                        SkuInfo skuInfo1 = new SkuInfo();
                        //把控对象存储到redis
                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                    }
                    //不是空，把从数据库读取到数据存储redis缓存
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    //释放锁
                    String script="if redis.call('get',KEYS[1]) == ARGV[1] "+
                            "then "+
                            "   return redis.call('del',KEYS[1]) "+
                            "else  "+
                            "   return 0 "+
                            "end";
    // 设置lua脚本返回的数据类型
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
    // 设置lua脚本返回类型为Long
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptText(script);
                    redisTemplate.execute(redisScript, Arrays.asList(lockKey),uuid);
                    return skuInfo;
                }else {
                    //未获取到锁
                    //等待一会
                    try {
                        Thread.sleep(1000);
                        return getSkuInfoRedis(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

            return skuInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return getSkuInfoDB(skuId);
    }

    //使用redisson实现分布式锁
    private SkuInfo getSkuInfoRedisson(Long skuId){
        SkuInfo skuInfo=null;

        try {
            //定义redis存储数据key
            String skuKey=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;

            //尝试从缓存读取数据
            skuInfo= (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //判断skuinfo是否为空
            if(skuInfo==null){
                //使用redisson获取锁
                String lockKey=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                //尝试加锁
                boolean islock = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if(islock){
                    try {
                        //获取到锁，读取数据库
                        skuInfo= getSkuInfoDB(skuId);
                        //判断从数据库读取数据是否为空
                        if(skuInfo==null){
                            SkuInfo skuInfo1 = new SkuInfo();
                            //把控对象存储到redis
                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);

                        }
                        //把从数据库读取到对象存储到redis
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);

                        return skuInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        //解锁
                        lock.unlock();
                    }
                }else {
                    //未获取到锁
                    try {
                        Thread.sleep(1000);
                        return getSkuInfoRedisson(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                //从缓存直接读取到数据
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return getSkuInfoDB(skuId);
    }

    @OmallCache(prefix = "BaseCategoryList")
    @Override
    public List<JSONObject> getBaseCategoryList() {
        //创建一个总的list集合封装json对象
        List<JSONObject> list=new ArrayList<>();

        //调用分类视图，获取全部分类数据
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        //流加工整理数据集合
        //按照一级分类编号，分组
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        //定义一个变量分类级别
        int index=1;
        //遍历一级分类map
        for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {
            //获取一级分类编号
          Long category1Id=  entry.getKey();
          //获取value 二级分类集合
            List<BaseCategoryView> category2List=    entry.getValue();
            //创建一个存储一级分类json对象
            JSONObject category1 = new JSONObject();
            //把对象属性封装
            category1.put("categoryId",category1Id);
            category1.put("categoryName",category2List.get(0).getCategory1Name());
            category1.put("index",index);

            //把index++
            index++;

            //把二级分类集合数据，使用流加工处理，按照二级分类编号进行分组
            Map<Long, List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            //创建一个集合存储二级分类数据
            List<JSONObject> category2Child=new ArrayList<>();
            //遍历二级分类分组结果map
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()) {
                //获取二级分类编号
                Long category2Id = entry2.getKey();
                //获取value 三级分类集合数据
                List<BaseCategoryView> category3List = entry2.getValue();
                //创建一个json对象，封装二级分类数据
                JSONObject category2 = new JSONObject();
                //设置二级分类对象属性
                category2.put("categoryId",category2Id);
                category2.put("categoryName",category3List.get(0).getCategory2Name());
                //把二级分类对象加入到二级分类集合
                category2Child.add(category2);

                //创建一个集合存储三级分类集合
                List<JSONObject> category3Child=new ArrayList<>();
                //把category3List 循环变量
               category3List.stream().forEach(category3View->{
                   //创建一个json对象，存储三级分类
                   JSONObject category3 = new JSONObject();
                   category3.put("categoryId",category3View.getCategory3Id());
                   category3.put("categoryName",category3View.getCategory3Name());
                   category3Child.add(category3);
               });

               //把三级分类集合加入到二级分类对象
                category2.put("categoryChild",category3Child);
            }

            //把🎧分类集合加入到一级分类对象
            category1.put("categoryChild",category2Child);
           //把一级分类对象，加入总的list集合
            list.add(category1);
        }


        return list;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }
}
