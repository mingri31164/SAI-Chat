package com.mingri.handler;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mingri.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
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

    /**
     * @Description: 此处时间不能先赋值再填充,否则会导致时间不变
     * @Author: mingri31164
     * @Date: 2025/1/28 13:13
     **/
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", new Date(), metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);
        this.setFieldValByName("createBy", BaseContext.getCurrentId(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }


}
