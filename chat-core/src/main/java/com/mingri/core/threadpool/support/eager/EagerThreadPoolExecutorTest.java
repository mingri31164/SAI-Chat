package com.mingri.core.threadpool.support.eager;

import cn.hutool.core.thread.ThreadUtil;
import com.mingri.core.threadpool.build.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EagerThreadPoolExecutorTest {

    public static void main(String[] args) {
        ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
        TaskQueue taskQueue = new TaskQueue<>(1);
        ThreadFactory threadFactory = ThreadFactoryBuilder.builder().prefix("mingri").daemon(false).build();
        EagerThreadPoolExecutor eagerThreadPoolExecutor =
                new EagerThreadPoolExecutor(1, 3,
                        1024, TimeUnit.SECONDS, taskQueue, threadFactory ,abortPolicy);
        taskQueue.setExecutor(eagerThreadPoolExecutor);
        for (int i = 0; i < 5; i++) {
            try {
                eagerThreadPoolExecutor.execute(() -> ThreadUtil.sleep(100000L));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        System.out.println("================ 线程池拒绝策略执行次数: " + eagerThreadPoolExecutor.getRejectCount());
    }
}