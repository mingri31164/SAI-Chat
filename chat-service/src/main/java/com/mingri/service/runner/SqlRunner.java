package com.mingri.service.runner;

import com.mingri.service.user.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SqlRunner implements ApplicationRunner {

    @Resource
    UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userService.allUserOffline();
    }
}
