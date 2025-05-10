package com.mingri.middleware.dynamic.thread.pool.sdk.registry;


import com.mingri.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @Description: Redis 注册中心接口
 * @Author: mingri31164
 * @Date: 2024/10/15 4:04
 **/
public interface IRegistry {

    /**
     * @Description: 上报整个线程池
     * @Author: mingri31164
     * @Date: 2024/10/15 4:20
     **/
    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities);

    /**
     * @Description: 上报线程池参数
     * @Author: mingri31164
     * @Date: 2024/10/15 4:20
     **/
    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);

}
