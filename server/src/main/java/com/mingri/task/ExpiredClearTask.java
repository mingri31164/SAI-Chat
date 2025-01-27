package com.mingri.task;


import com.mingri.service.IMessageService;
import com.mingri.service.ISysUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

@Component
public class ExpiredClearTask {

    @Resource
    IMessageService messageService;

    @Resource
    ISysUserService userService;

    @Value("${mini-chat.expires}")
    int expirationDays;


    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredContent() {
        LocalDate expirationDate = LocalDate.now().minusDays(expirationDays);
        messageService.deleteExpiredMessages(expirationDate);
        userService.deleteExpiredUsers(expirationDate);
    }
}
