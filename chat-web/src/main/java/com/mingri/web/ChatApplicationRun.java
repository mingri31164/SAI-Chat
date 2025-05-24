package com.mingri.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@Configurable
@EnableAsync
@Import(RocketMQAutoConfiguration.class)
public class ChatApplicationRun {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplicationRun.class, args);
        log.info("server started");
    }

}
