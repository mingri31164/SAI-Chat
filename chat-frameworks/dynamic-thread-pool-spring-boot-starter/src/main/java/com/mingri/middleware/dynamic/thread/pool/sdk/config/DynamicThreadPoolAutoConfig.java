package com.mingri.middleware.dynamic.thread.pool.sdk.config;

import com.mingri.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import com.mingri.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.mingri.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.mingri.middleware.dynamic.thread.pool.sdk.domain.model.vo.RegistryEnumVO;
import com.mingri.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import com.mingri.middleware.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import com.mingri.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.mingri.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.apache.commons.lang.StringUtils;



/**
 * @description: 动态配置入口
 * @author: mingri
 * @date: 2024/10/13 14:39
 **/
@Configuration
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
@EnableScheduling
@Slf4j
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private String applicationName;


    @Bean("dynamicThreadRedissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;

        RedissonClient redissonClient = Redisson.create(config);

        logger.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }

    @Bean
    public IRegistry redisRegistry(RedissonClient dynamicThreadRedissonClient) {
        return new RedisRegistry(dynamicThreadRedissonClient);
    }

    @Bean("dynamicThreadPollService")
    public DynamicThreadPoolService dynamicThreadPollService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap, RedissonClient redissonClient) {

        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if (StringUtils.isBlank(applicationName)) {
            applicationName = "缺省的";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }

        // 获取缓存数据，设置本地线程池配置
        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : threadPoolKeys) {
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolKey).get();
            if (null == threadPoolConfigEntity) continue;
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }


    /**
     * @Description: 线程池上报redis
     * @Author: mingri31164
     * @Date: 2024/10/16 20:50
     **/
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(DynamicThreadPoolService dynamicThreadPoolService, IRegistry registry)
    {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry);
    }


    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }


    /**
     * @Description:
     * @Author: mingri31164
     * @Date: 2024/10/17 0:52
     **/
    @Bean(name = "dynamicThreadPoolRedisTopic")
    public RTopic registerThreadPoolRedisTopic
    (RedissonClient redissonClient,
     ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        RTopic topic = redissonClient.getTopic
                (RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        topic.addListener(ThreadPoolConfigEntity.class, threadPoolConfigAdjustListener);
        return topic;
    }}
