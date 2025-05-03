package com.mingri.annotation;

import com.mingri.constant.type.LimitKeyType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * @Description: 接口限流
 * @Author: mingri31164
 * @Date: 2025/1/31 20:59
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlLimit {
    LimitKeyType keyType() default LimitKeyType.ID; //限制类型，ip或者id

    int maxRequests() default 60; //每分钟最大请求次数
}
