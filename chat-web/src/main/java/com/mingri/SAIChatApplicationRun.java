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

//    @Bean
//    public ApplicationRunner applicationRunner(ExecutorService threadPoolExecutor01) {
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

}
