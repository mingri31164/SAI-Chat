package com.mingri.core.threadpool.build;

import com.mingri.core.threadpool.support.eager.EagerThreadPoolExecutor;
import com.mingri.core.threadpool.support.eager.TaskQueue;
import lombok.Getter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程工厂 {@link EagerThreadPoolExecutor} 构建器, 构建者模式
 */
public class EagerThreadPoolExecutorBuilder {
    private int corePoolSize = 4;
    private int maximumPoolSize = 8;
    private long keepAliveTime = 60L;
    private TimeUnit unit = TimeUnit.SECONDS;
    private int queueCapacity = 1000;
    private ThreadFactory threadFactory = ThreadFactoryBuilder.builder().prefix("eager-pool").build();
    private RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.AbortPolicy();
    @Getter
    private AtomicLong rejectedNum = new AtomicLong();

    public static EagerThreadPoolExecutorBuilder builder() {
        return new EagerThreadPoolExecutorBuilder();
    }

    public EagerThreadPoolExecutorBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public EagerThreadPoolExecutorBuilder maximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public EagerThreadPoolExecutorBuilder keepAliveTime(long keepAliveTime, TimeUnit unit) {
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        return this;
    }

    public EagerThreadPoolExecutorBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    public EagerThreadPoolExecutorBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public EagerThreadPoolExecutorBuilder rejectedHandler(RejectedExecutionHandler handler) {
        this.rejectedHandler = handler;
        return this;
    }

    public EagerThreadPoolExecutor build() {
        TaskQueue<Runnable> taskQueue = new TaskQueue<>(queueCapacity);
        return new EagerThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                taskQueue,
                threadFactory,
                rejectedHandler,
                rejectedNum
        );
    }
}