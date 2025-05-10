package com.mingri.annotation;

import java.lang.annotation.*;



@Documented
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncExecute {


}
