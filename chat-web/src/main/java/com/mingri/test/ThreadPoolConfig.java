package com.mingri.test;

import com.mingri.core.threadpool.build.EagerThreadPoolExecutorBuilder;
import com.mingri.core.threadpool.support.eager.EagerThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {

    @Bean("threadPoolExecutor01")
    public EagerThreadPoolExecutor threadPoolExecutor01(ThreadPoolConfigProperties properties) {
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
        return EagerThreadPoolExecutorBuilder.builder()
                .corePoolSize(properties.getCorePoolSize())
                .maximumPoolSize(properties.getMaxPoolSize())
                .keepAliveTime(properties.getKeepAliveTime(), TimeUnit.SECONDS)
                .queueCapacity(properties.getBlockQueueSize())
                .threadFactory(Executors.defaultThreadFactory())
                .rejectedHandler(handler)
                .build();
    }

    @Bean("threadPoolExecutor02")
    public EagerThreadPoolExecutor threadPoolExecutor02() {

        int core = 1;
        int max = 4;
        int queue = 2;

        return EagerThreadPoolExecutorBuilder.builder()
                .corePoolSize(core)
                .maximumPoolSize(max)
                .queueCapacity(queue)
                .keepAliveTime(10, TimeUnit.SECONDS)
                .build();

    }

}
