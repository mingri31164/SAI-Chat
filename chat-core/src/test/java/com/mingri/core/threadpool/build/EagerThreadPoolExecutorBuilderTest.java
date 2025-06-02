package com.mingri.core.threadpool.build;

import com.mingri.core.threadpool.support.eager.EagerThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
class EagerThreadPoolExecutorBuilderTest {

    // 测试拒绝策略代理
    @Test
    void testBuildAndExecute() throws InterruptedException {
        EagerThreadPoolExecutor executor = EagerThreadPoolExecutorBuilder.builder()
                .corePoolSize(1)
                .maximumPoolSize(1)
                .queueCapacity(1)
                .keepAliveTime(10, TimeUnit.SECONDS)
                .build();

        int totalTasks = 10;
        int rejectedCount = 0;
        for (int i = 0; i < totalTasks; i++) {
            try {
                executor.execute(() -> {
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                });
            } catch (RejectedExecutionException e) {
                rejectedCount++;
            }
        }

        // 等待任务执行完毕
        Thread.sleep(1000);
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("================ 线程池拒绝策略执行次数: " + executor.getRejectedNum());
        System.out.println("================ 代码捕获到的拒绝次数: " + rejectedCount);
    }


    // 测试快速消费
    @Test
    void testEagerThreadPoolExpansion() throws InterruptedException {
        int core = 1;
        int max = 4;
        int queue = 2;
        EagerThreadPoolExecutor executor = EagerThreadPoolExecutorBuilder.builder()
                .corePoolSize(core)
                .maximumPoolSize(max)
                .queueCapacity(queue)
                .keepAliveTime(10, TimeUnit.SECONDS)
                .build();

        int totalTasks = 12;
        int rejectedCount = 0;
        for (int i = 0; i < totalTasks; i++) {
            try {
                executor.execute(() -> {
                    try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                });
                System.out.printf("[提交第%d个任务] 当前线程池线程数: %d, " +
                                "队列长度: %d, 队列中的任务数: %d, 已完成任务数: %d, 拒绝次数: %d\n",
                        i + 1, executor.getPoolSize(), executor.getQueue().size(),
                        executor.getQueue().size(), executor.getCompletedTaskCount(), executor.getRejectedNum());
            } catch (RejectedExecutionException e) {
                rejectedCount++;
                System.out.printf("[提交第%d个任务] 被拒绝! 当前线程池线程数: %d, " +
                                "队列长度: %d, 队列中的任务数: %d, 已完成任务数: %d, 拒绝次数: %d\n",
                        i + 1, executor.getPoolSize(), executor.getQueue().size(),
                        executor.getQueue().size(), executor.getCompletedTaskCount(), executor.getRejectedNum());
            }
        }

        // 任务提交后，分阶段打印线程池状态
        for (int t = 1; t <= 4; t++) {
            Thread.sleep(400);
            System.out.printf("[第%d次阶段性检查] 线程池线程数: %d, " +
                            "队列长度: %d, 队列中的任务数: %d, 已完成任务数: %d, 拒绝次数: %d\n",
                    t, executor.getPoolSize(), executor.getQueue().size(),
                    executor.getQueue().size(), executor.getCompletedTaskCount(), executor.getRejectedNum());
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("================ 线程池最终拒绝策略执行次数: " + executor.getRejectedNum());
        System.out.println("================ 代码捕获到的最终拒绝次数: " + rejectedCount);
    }
}