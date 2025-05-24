package com.mingri.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


@AutoConfiguration
@ComponentScan("com.mingri.service")
@MapperScan(basePackages = {
        "com.mingri.service.notify.repo.mapper",
        "com.mingri.service.user.repo.mapper",
        "com.mingri.service.chat.repo.mapper",
        "com.mingri.service.talk.repo.mapper",
        "com.mingri.service.admin.repo.mapper"
})
public class ServiceAutoConfig {
}
