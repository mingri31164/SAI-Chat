package com.mingri.service;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;


@AutoConfiguration
@ComponentScan("com.mingri.service")
@MapperScan(basePackages = {
        "com.mingri.service.notify.repo.mapper",
        "com.mingri.service.user.repo.mapper",
        "com.mingri.service.chat.repo.mapper",
        "com.mingri.service.talk.repo.mapper",
        "com.mingri.service.admin.repo.mapper"
})
@Import(RocketMQAutoConfiguration.class)
public class ServiceAutoConfig {

}
