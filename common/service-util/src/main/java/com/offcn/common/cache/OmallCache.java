package com.offcn.common.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})//注解使用位置
@Retention(RetentionPolicy.RUNTIME)
public @interface OmallCache {

    //定义缓存数据前缀
    String prefix() default "cache";
}
