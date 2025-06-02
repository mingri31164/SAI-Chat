package com.mingri.core.sensitive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: qing
 * @Date: 2024/8/3
 * @description: 对mybatis查询出来的敏感词进行过滤
 *               对字段插入会敏感词时需自行通过sensitiveService.replace进行过滤
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SensitiveField {

    /**
     * 需要脱敏的字段
     * @return
     */
    String bind() default "";
}
