package com.mingri.config;

import com.mingri.middleware.dynamic.thread.pool.sdk.domain.model.vo.RegistryEnumVO;
import com.mingri.properties.ThreadPoolConfigProperties;
import com.mingri.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {

    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RedisUtils redisUtils;

    @Bean(value = "douBao",destroyMethod = "shutdown")
    public ThreadPoolExecutor threadPoolExecutor01(ThreadPoolConfigProperties properties) {
        // 实例化策略
        RejectedExecutionHandler handler;
        switch (properties.getPolicy()){
            case "AbortPolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }

        // 创建线程池
        return new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler);
    }


    @PreDestroy
    public void destroy() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
            try {
                if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPoolExecutor.shutdownNow();
                    redisUtils.del(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey());
                }
            } catch (InterruptedException e) {
                threadPoolExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
