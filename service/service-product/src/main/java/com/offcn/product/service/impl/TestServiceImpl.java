package com.offcn.product.service.impl;

import com.offcn.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    //注入redis模板工具对象
    @Autowired
   // private RedisTemplate redisTemplate;
    private StringRedisTemplate redisTemplate;

    //注入redisson
    @Autowired
    private RedissonClient redissonClient;

  /*  @Override
    public  void testLock() {
        //从redis读取num值
     String value= (String) redisTemplate.boundValueOps("num").get();
   //判断从redis读取的value是否为空
        if(StringUtils.isEmpty(value)){
            return;
        }
        //把读取到数值，转换成Integer
      int num=  Integer.parseInt(value);
        //把数值+1存储回redis
        redisTemplate.opsForValue().set("num",String.valueOf(++num));

    }*/
   /* @Override
    public  void testLock() {
        //生成一个随机锁值
        String uuid = UUID.randomUUID().toString();
        //尝试设置redis表示锁的那个key 名字lock
        //设置一个值，如果设置值成功返回true 表示我们获取到锁  获取锁时候设置锁有效期
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);

        if(lock) {
            //从redis读取num值
            String value = (String) redisTemplate.boundValueOps("num").get();
            //判断从redis读取的value是否为空
            if (StringUtils.isEmpty(value)) {
                return;
            }
            //出现异常
           //int i=1/0;
            //把读取到数值，转换成Integer
            int num = Integer.parseInt(value);
            //把数值+1存储回redis
            redisTemplate.opsForValue().set("num", String.valueOf(++num));

            //当我们处理完业务逻辑，释放锁
           *//* //尝试读取锁的值
            String lockValue = redisTemplate.opsForValue().get("lock");
            //比对当前读取到锁的值和生成uuid是否相同
            if(uuid.equals(lockValue)) {
                redisTemplate.delete("lock");
            }*//*
            //定义lua脚本
            //  定义一个lua 脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            //  准备执行lua 脚本
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            //  将lua脚本放入DefaultRedisScript 对象中
            redisScript.setScriptText(script);
            //  设置DefaultRedisScript 这个对象的泛型
            redisScript.setResultType(Long.class);

            //执行lua脚本
            redisTemplate.execute(redisScript, Arrays.asList("lock"),uuid);
        }else {
            //等待一会儿
            try {
                Thread.sleep(100);
                //再次尝试执行业务 自旋锁 效率问题
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }*/
  @Override
  public  void testLock() {
      RLock lock = redissonClient.getLock("lock");

      //调用锁对象，尝试加锁
     // lock.lock(3,TimeUnit.SECONDS);t
      //尝试去获取锁,最大等待100秒(多次尝试)
      //可重入锁：以线程为单位
      try {
          lock.tryLock(100,1,TimeUnit.SECONDS);
      } catch (InterruptedException e) {
          e.printStackTrace();
      }

      //从redis读取num值
          String value = (String) redisTemplate.boundValueOps("num").get();
          //判断从redis读取的value是否为空
          if (StringUtils.isEmpty(value)) {
              return;
          }
          //出现异常
          //int i=1/0;
          //把读取到数值，转换成Integer
          int num = Integer.parseInt(value);
          //把数值+1存储回redis
          redisTemplate.opsForValue().set("num", String.valueOf(++num));

        //释放锁
      //lock.unlock();



  }

    @Override
    public String readLock() {
        RReadWriteLock readwritelock = redissonClient.getReadWriteLock("readwritelock");
        //从读写锁 获取读锁
        RLock rLock = readwritelock.readLock();
        rLock.lock(10,TimeUnit.SECONDS);//尝试获取读锁，有效期是10秒
        //读取redis资源
        String msg = redisTemplate.opsForValue().get("msg");

        return msg;
    }

    @Override
    public String writeLock() {
        RReadWriteLock readwritelock = redissonClient.getReadWriteLock("readwritelock");
        RLock rLock = readwritelock.writeLock();
        rLock.lock(10,TimeUnit.SECONDS);
        //向redis写入数据
        redisTemplate.opsForValue().set("msg",UUID.randomUUID().toString());
        return "写入成功";
    }
}
