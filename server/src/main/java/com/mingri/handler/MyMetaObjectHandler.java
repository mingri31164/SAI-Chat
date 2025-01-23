package com.mingri.handler;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mingri.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
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
    LocalDateTime now = LocalDateTime.now();
    Long currentId = BaseContext.getCurrentId();

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        log.info("创建人：{}",currentId);
        this.strictInsertFill(metaObject, "createBy", Long.class, currentId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, now);
        this.strictUpdateFill(metaObject, "updateBy", Long.class, currentId);
    }

}
