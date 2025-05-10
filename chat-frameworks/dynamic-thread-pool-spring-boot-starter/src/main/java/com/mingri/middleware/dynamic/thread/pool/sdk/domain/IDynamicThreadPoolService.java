package com.mingri.middleware.dynamic.thread.pool.sdk.domain;


import com.mingri.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @Author: mingri31164
 * @CreateTime: 2024-10-13 15:35
 * @Description: 动态线程池服务
 * @Version: 1.0
 */

public interface IDynamicThreadPoolService {
    /**
     * @Description: 查询线程池配置集合
     * @Author: mingri31164
     * @Date: 2024/10/13 17:04
     **/
    List<ThreadPoolConfigEntity> queryThreadPoolList();

    /**
     * @Description: 根据名称查询线程池配置
     * @Author: mingri31164
     * @Date: 2024/10/13 17:09
     **/
    ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName);

    /**
     * @Description: 更新线程池配置
     * @Author: mingri31164
     * @Date: 2024/10/13 17:12
     **/
    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);
}
