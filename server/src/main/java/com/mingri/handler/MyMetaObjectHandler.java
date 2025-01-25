package com.mingri.handler;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.mingri.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/22 20:30
 * @ClassName: MyMetaObjectHandler
 * @Version: 1.0
 */

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    //准备赋值的数据
    Date now = new Date();
    Long currentId = BaseContext.getCurrentId();

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", now, metaObject);
        this.setFieldValByName("updateTime", now, metaObject);
        this.setFieldValByName("createBy", currentId, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", now, metaObject);
    }


}
