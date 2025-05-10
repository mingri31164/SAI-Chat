package com.mingri.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;



@Component
@Documented
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyEventListener {

    /**
     * 是否异步执行，默认否
     */
    boolean async() default false;

}
