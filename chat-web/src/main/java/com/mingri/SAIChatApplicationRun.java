package com.mingri;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.mingri.core.threadpool.support.eager.EagerThreadPoolExecutor;


@Slf4j
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@Configurable
@EnableAsync
public class SAIChatApplicationRun {
    public static void main(String[] args) {
        SpringApplication.run(SAIChatApplicationRun.class, args);
        log.info("server started");
    }




    // 动态线程池测试
//    @Bean
//    public ApplicationRunner applicationRunner1(ExecutorService threadPoolExecutor01) {
//        return args -> {
//            while (true){
//                // 创建一个随机时间生成器
//                Random random = new Random();
//                // 随机时间，用于模拟任务启动延迟
//                int initialDelay = random.nextInt(10) + 1; // 1到10秒之间
//                // 随机休眠时间，用于模拟任务执行时间
//                int sleepTime = random.nextInt(10) + 1; // 1到10秒之间
//
//                // 提交任务到线程池
//                threadPoolExecutor01.submit(() -> {
//                    try {
//                        // 模拟任务启动延迟
//                        TimeUnit.SECONDS.sleep(initialDelay);
//                        System.out.println("Task started after " + initialDelay + " seconds.");
//
//                        // 模拟任务执行
//                        TimeUnit.SECONDS.sleep(sleepTime);
//                        System.out.println("Task executed for " + sleepTime + " seconds.");
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                });
//
//                Thread.sleep(random.nextInt(50) + 1);
//            }
//        };
//    }




    // 动态线程池+快速消费线程池测试
//    @Bean
//    public ApplicationRunner applicationRunner2(EagerThreadPoolExecutor threadPoolExecutor02) {
//        return args -> {
//            // 提交大量任务，观察线程池扩容与队列变化
//            for (int i = 0; i < 20; i++) {
//                int taskId = i;
//                threadPoolExecutor02.submit(() -> {
//                    try {
//                        int sleepTime = new Random().nextInt(3) + 1;
//                        System.out.printf("[任务%d] 执行中，线程：%s，休眠%d秒\n", taskId, Thread.currentThread().getName(), sleepTime);
//                        TimeUnit.SECONDS.sleep(sleepTime);
//                        System.out.printf("[任务%d] 完成，线程：%s\n", taskId, Thread.currentThread().getName());
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                });
//                // 每次提交后打印线程池状态
//                System.out.printf("[提交任务%d] 核心线程数：%d，最大线程数：%d，当前线程数：%d，队列长度：%d\n",
//                        i,
//                        threadPoolExecutor02.getCorePoolSize(),
//                        threadPoolExecutor02.getMaximumPoolSize(),
//                        threadPoolExecutor02.getPoolSize(),
//                        threadPoolExecutor02.getQueue().size()
//                );
//                Thread.sleep(200); // 控制提交速度，便于观察
//            }
//        };
//    }
}
