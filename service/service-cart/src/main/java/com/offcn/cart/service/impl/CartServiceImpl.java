package com.offcn.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.cart.mapper.CartInfoMapper;
import com.offcn.cart.service.CartAsyncService;
import com.offcn.cart.service.CartService;
import com.offcn.common.constant.RedisConst;
import com.offcn.common.result.Result;
import com.offcn.common.util.DateUtil;
import com.offcn.model.cart.CartInfo;
import com.offcn.model.product.SkuInfo;
import com.offcn.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    //注入购物车数据操作接口
    @Autowired
    private CartInfoMapper cartInfoMapper;

    //注入redis工具对象
    @Autowired
    private RedisTemplate redisTemplate;

    //注入商品微服务feign接口
    @Autowired
    private ProductFeignClient productFeignClient;

    //注入异步执行服务
    @Autowired
    private CartAsyncService cartAsyncService;

    @Override
    public void addToCart(Long skuId, Integer skuNum, String userId) {

        //首先获取redis缓存中购物车数据库key
        String cartKey = getCartKey(userId);

        //判断redis缓存中是否存在该key
        if(!redisTemplate.hasKey(cartKey)){
            //调用读取数据库中购物车数据，写入缓存方法
            loadCartCache(userId);
        }

        //尝试从缓存中读取指定sku编号的购物车对象
      CartInfo cartInfo= (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId.toString());
        //判断该购物车对象是否为空
        if(cartInfo!=null){
            //更新购买商品数量
            cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
            //调用商品服务feign接口获取当前最新价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            //把最新价格设置到当前购物车对象
            cartInfo.setSkuPrice(skuPrice);
            cartInfo.setCartPrice(skuPrice);

            //更新设置更新时间
            cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
            //购物车商品选中状态 1 选中
            cartInfo.setIsChecked(1);
            //更新保存购物车数据到数据库
           // cartInfoMapper.updateById(cartInfo);
            //调用异步服务，更新到数据库
            cartAsyncService.updateCartInfo(cartInfo);

        }else {
            //如果从缓存获取购物车数据为空
            //创建一个空购物车对象
            cartInfo=new CartInfo();
            //根据sku编号，调用商品服务feign接口，获取最新sku商品信息
            Result<SkuInfo> skuInfoResult = productFeignClient.getSkuInfo(skuId);
            SkuInfo skuInfo = skuInfoResult.getData();
            //设置sku编号
            cartInfo.setSkuId(skuInfo.getId());
            //设置购买用户编号
            cartInfo.setUserId(userId);
            //调用商品服务feign接口读取最新价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            cartInfo.setCartPrice(skuPrice);
            cartInfo.setSkuPrice(skuPrice);

            //设置购买数量
            cartInfo.setSkuNum(skuNum);
            //设置配图
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            //设置sku名称
            cartInfo.setSkuName(skuInfo.getSkuName());
            //选中状态 1
            cartInfo.setIsChecked(1);
            //设置创建时间当前时间
            cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
            //设置更新时间
            cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));

            //把购物车对象存储到数据库
            //cartInfoMapper.insert(cartInfo);
            //采用异步新增购物车数据到数据库
            cartAsyncService.saveCartInfo(cartInfo);
        }

        //更新最新购物车数据到redis缓存，保证数据库数据和缓存数据一致性
        redisTemplate.boundHashOps(cartKey).put(skuId.toString(),cartInfo);
        //设置过期时间
        setCartKeyExpire(cartKey);

    }

    //定义一个方法：从数据库加载数据写入到redis缓存
    @Override
    public List<CartInfo> loadCartCache(String userId){
       //首先创建购物车表查询条件
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        //设置查询条件：user_id 用户id
        queryWrapper.eq("user_id",userId);
        //按照条件去数据库查询数据
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(queryWrapper);
        //判断数据库集合是否为空
        if(CollectionUtils.isEmpty(cartInfoList)){
            return cartInfoList;
        }

        //创建一个Map集合存储当前用户全部的购物车数据
        Map<String,CartInfo> map=new HashMap<>();

        //不为空，要把从数据库读取到结果集合遍历
        for (CartInfo cartInfo : cartInfoList) {
            //使用商品微服务feign接口，获取商品最新价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
            //把最新价格设置到购物车对象
            cartInfo.setSkuPrice(skuPrice);
            cartInfo.setCartPrice(skuPrice);
            //逐个调用redis工具对象，存储到redis
            //把每个购车对象存储到map，为了批量操作redis存储
            map.put(cartInfo.getSkuId().toString(),cartInfo);
        }

        //批量存储购物车数据到redis
        //获取redis存储购物车数据key
        String cartKey = getCartKey(userId);
        redisTemplate.opsForHash().putAll(cartKey,map);//批量存储到redis
        //设置购物车key，redis缓存有效期
        setCartKeyExpire(cartKey);

        return cartInfoList;
    }

    //定义一个获取购物车在redis存储的key的名称方法
    private String getCartKey(String userId){
        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }

    //定义一个设置指定key的reids有效期方法
    private void setCartKeyExpire(String cartKey){
        redisTemplate.expire(cartKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        List<CartInfo> cartInfoList=new ArrayList<>();
        //判断userId是否为空 表示用户未登录
        if(StringUtils.isEmpty(userId)){
            //使用临时用户，获取未登录购物车数据
            cartInfoList=  getCartList(userTempId);
            return cartInfoList;
        }

        //判断userId不为空，表示用户已经登录
        if(!StringUtils.isEmpty(userId)){
            //当用户处于登录状态，尝试获取未登录购物车数据集合
            List<CartInfo> cartInfoNoLoginList = getCartList(userTempId);
            //判断未登录购物车集合是否为空
            if(!CollectionUtils.isEmpty(cartInfoNoLoginList)){
                //调用合并购物车方法
                mergeToCartList(cartInfoNoLoginList,userId);
                //删除未登录购物车数据
                deleteCartList(userTempId);

                //重新加载数据库数据到缓存
                cartInfoList=   loadCartCache(userId);

            }

            //判断userTempId如果是空  cartInfoList 是空
            if(StringUtils.isEmpty(userTempId)||CollectionUtils.isEmpty(cartInfoNoLoginList)){
                cartInfoList=  getCartList(userId);
            }


        }

        return cartInfoList;

    }

    //定义一个删除购物车数据方法
    private void deleteCartList(String userTempId){
      /*  //创建删除条件
        QueryWrapper<CartInfo> deleteWrapper = new QueryWrapper<>();
        //设置删除条件
        deleteWrapper.eq("user_id",userTempId);
        cartInfoMapper.delete(deleteWrapper);*/


        //调用异步服务执行删除
        cartAsyncService.deleteCartInfo(userTempId);
        //获取缓存中key
        String cartKey = getCartKey(userTempId);

        //判断缓存中是否存在该key
        if(redisTemplate.hasKey(cartKey)){
            //调用reis工具对象，删除指定key
            redisTemplate.delete(cartKey);
        }
    }

    //定义一个方法：根据指定用户id获取购物车数据
    private List<CartInfo> getCartList(String userId){
        List<CartInfo> cartInfoList=new ArrayList<>();

        //判断用户id是否为空
        if(StringUtils.isEmpty(userId)){
            return cartInfoList;
        }

        //获取缓存中存储key
        String cartKey = getCartKey(userId);
        //尝试去缓存读取数据
        cartInfoList = redisTemplate.boundHashOps(cartKey).values();
        //判断缓存中读取购物车集合数据为空
        if(!CollectionUtils.isEmpty(cartInfoList)){
            //返回数据集合之前，处理排序 按照加入购物车时间（更新时间）降序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return DateUtil.truncatedCompareTo(o1.getUpdateTime(),o2.getUpdateTime(),Calendar.SECOND);
                }
            });

            return cartInfoList;
        }else {
            //从缓存中读取购物车集合数据为空
            //调用读取数据库，写入缓存方法
          cartInfoList=  loadCartCache(userId);
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return DateUtil.truncatedCompareTo(o1.getUpdateTime(),o2.getUpdateTime(),Calendar.SECOND);
                }
            });
        }


        return cartInfoList;
    }

    //定义一个合并购物车方法
    private List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList,String userId){
        //获取登录用户购物车集合数据
        List<CartInfo> cartInfoLoginList = getCartList(userId);

        //把登录用户购物车集合转换成map
        Map<Long, CartInfo> cartInfoMap = cartInfoLoginList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));

        for (CartInfo cartInfo : cartInfoNoLoginList) {
            //获取skuId
            Long skuId = cartInfo.getSkuId();
            //判断skuId在map里面是否存在
            if(cartInfoMap.containsKey(skuId)){
                //获取登录购物车集合 购物车对象
                CartInfo cartInfoLogin = cartInfoMap.get(skuId);
                //更新购买数量=现有购买数量+未登录购买数量
                cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum()+cartInfo.getSkuNum());
                //修改更新时间
                cartInfoLogin.setUpdateTime(new Timestamp(new Date().getTime()));
                //判断未登录购物车的选中状态是否等于1
                if(cartInfo.getIsChecked().intValue()==1){
                    //也要把登录购物车状态修改为 1选中
                    cartInfoLogin.setIsChecked(1);
                }

                //创建更新到数据库条件
                QueryWrapper<CartInfo> updateWrapper = new QueryWrapper<>();
                //设置更新条件1 skuId
                updateWrapper.eq("sku_id",cartInfoLogin.getSkuId());
                //设置更新条件2：userId
                updateWrapper.eq("user_id",cartInfoLogin.getUserId());

                //按照设置更新条件更新保存购物车数据到数据库
                cartInfoMapper.update(cartInfoLogin,updateWrapper);
            }else {
                //未登录购物车商品，在已经登录购物车集合不存在
                //设置购物车属性1：userId
                cartInfo.setUserId(userId);
                //设置创建时间
                cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                //设置更新时间
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                //把未登录购物车数据插入到数据库
                cartInfoMapper.insert(cartInfo);
            }
        }

        //重新读取数据库购物车数据，更新写入缓存
        List<CartInfo> cartInfoList = loadCartCache(userId);

        return cartInfoList;

    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        //调用异步方法，修改数据库商品状态
        cartAsyncService.checkCart(userId, isChecked, skuId);

        //修改redis缓存中商品选中状态

        //获取缓存中key名字
        String cartKey = getCartKey(userId);

        //使用key去操作缓存中指定数据
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);

        //判断在hash指定skuid购物车数据是否存在
        if(boundHashOperations.hasKey(skuId.toString())){
            //获取指定skuId的购物车数据对象
            CartInfo cartInfo = boundHashOperations.get(skuId.toString());
            //修改购物车对象的是否选中状态
            cartInfo.setIsChecked(isChecked);
            //把更新状态购物车对象，更新存储回缓存
            boundHashOperations.put(skuId.toString(),cartInfo);
            //更新缓存过期时间
            setCartKeyExpire(cartKey);
        }
    }

    @Override
    public void deleteCart(String userId, Long skuId) {
        //首先调用异步服务，删除数据库中购物车商品
        cartAsyncService.deleteCartInfo(userId,skuId);

        //删除缓存中秒杀商品
        String cartKey = getCartKey(userId);

        //创建redis指定key的数据操作对象
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //判断指定skuid的商品是否存在
        if(boundHashOperations.hasKey(skuId.toString())){
            //删除指定sku编号商品
            boundHashOperations.delete(skuId.toString());
        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList=new ArrayList<>();

        //获取缓存key
        String cartKey = getCartKey(userId);
        //获取指定userid的对应全部购物车数据
        List<CartInfo> cartCacheInfoList = redisTemplate.boundHashOps(cartKey).values();

        //判断从缓存读取购物车集合是否为空
        if(!CollectionUtils.isEmpty(cartCacheInfoList)) {
            //遍历购物车集合
            for (CartInfo cartInfo : cartCacheInfoList) {
                //判断商品选中状态是否等于 1
                if(cartInfo.getIsChecked().intValue()==1){
                    //把选中状态购物车对象存储到返回集合
                    cartInfoList.add(cartInfo);
                }
            }

        }

        return cartInfoList;
    }
}
