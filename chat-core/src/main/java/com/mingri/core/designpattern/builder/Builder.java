package com.mingri.core.designpattern.builder;

import java.io.Serializable;

/**
 * Builder 构造者模式抽象接口
 */
public interface Builder<T> extends Serializable {

    /**
     * 构建方法
     */
    T build();
}
