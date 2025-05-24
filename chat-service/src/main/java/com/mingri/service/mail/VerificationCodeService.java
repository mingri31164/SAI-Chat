package com.mingri.service.mail;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.mingri.core.toolkit.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@Service
@Slf4j
public class VerificationCodeService {

    @Resource
    EmailService emailService;

    @Resource
    RedisUtils redisUtils;

    public void emailVerificationCode(String email) {
        String code = (String) redisUtils.get(email);
        if (code != null) {
            return;
        }
        Context context = new Context();
        context.setVariable("nowDate", DateUtil.now());
        code = RandomUtil.randomNumbers(6);
        redisUtils.set(email, code, 10 * 60);
        context.setVariable("code", code.toCharArray());
        emailService.sendHtmlMessage(email, "Mingri验证码", "email_template.html", context);
    }
}
