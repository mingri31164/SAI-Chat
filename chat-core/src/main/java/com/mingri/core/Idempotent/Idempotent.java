package com.mingri.core.Idempotent;

import java.lang.annotation.*;

/**
 * 幂等注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    String key() default "";
    long expire() default 4; // 锁过期时间（秒）
}