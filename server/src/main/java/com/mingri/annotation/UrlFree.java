package com.mingri.annotation;

import java.lang.annotation.*;


/**
 * @Description:
 * @Author: mingri31164
 * @Date: 2025/1/31 23:12
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UrlFree {
    String value() default "";
}
