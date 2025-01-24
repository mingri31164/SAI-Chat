package com.mingri.service.impl;

import cn.hutool.extra.mail.Mail;
import cn.hutool.extra.mail.MailAccount;
import com.mingri.constant.MailConstant;
import com.mingri.constant.MessageConstant;
import com.mingri.exception.EmailErrorException;
import com.mingri.properties.EmailProperties;
import com.mingri.service.CommonService;
import com.mingri.utils.RedisUtils;
import com.mingri.utils.VerifyCodeUtil;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/20 16:47
 * @ClassName: CaptchaServiceImpl
 * @Version: 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CommonService {

    private final EmailProperties emailProperties;
    private final RedisUtils redisUtils;
    private final TemplateEngine templateEngine;


    /**
     * 发送邮件验证码
     * @param email 邮箱
     */
    @Override
    public void sendEmailCaptcha(String email) {
        // 验证邮件配置是否完整
        validateEmailProperties();

        // 验证邮箱格式
        if (!VerifyCodeUtil.checkEmail(email)) {
            throw new EmailErrorException(MessageConstant.EMAIL_FORMAT_ERROR);
        }

        // 生成或获取验证码
        String captcha = getCaptcha(email);

        // 生成邮件内容
        String content = generateEmailContent(captcha);

        // 发送邮件
        List<String> list = Collections.singletonList(email);
        sendEmail(list, content);
    }

    /**
     * 判断邮件配置是否完整
     */
    private void validateEmailProperties() {
//        log.info("正在验证邮件配置是否完整...：{}",emailProperties);
        if (emailProperties.getUser() == null || emailProperties.getPassword() == null || emailProperties.getFrom() == null || emailProperties.getHost() == null || emailProperties.getPort() == null) {
            throw new EmailErrorException(MessageConstant.EMAIL_VERIFICATION_CODE_CONFIGURATION_EXCEPTION);
        }
    }

    /**
     * 获取验证码
     *
     * @param email 邮箱地址，用于生成和存储验证码。
     * @return {@link String} 返回生成的验证码。
     */
    private String getCaptcha(String email) {
        // 根据邮箱生成Redis键名
        String redisKey = MailConstant.CAPTCHA_CODE_KEY_PRE + email;
        // 尝试从Redis获取现有的验证码
        Object oldCode = redisUtils.get(redisKey);
        if (oldCode == null) {
            // 如果验证码不存在，生成新的验证码
            String captcha = VerifyCodeUtil.generateVerifyCode();
            // 将新生成的验证码存储到Redis，并设置过期时间
            boolean saveResult = redisUtils.set(redisKey, captcha, emailProperties.getExpireTime());
            if (!saveResult) {
                // 如果存储失败，抛出异常
                throw new RedisException(MessageConstant.EXCEPTION_VERIFICATION_CODE_SAVE_FAILED);
            }
            return captcha;
        } else {
            // 如果验证码存在，重置其在Redis中的过期时间
            boolean expireResult = redisUtils.expire(redisKey, emailProperties.getExpireTime());
            if (!expireResult) {
                throw new RedisException(MessageConstant.RESET_VERIFICATION_CODE_FAILED);
            }
            return String.valueOf(oldCode);
        }
    }


    /**
     * 生成邮件内容
     * @param captcha 验证码
     * @return {@link String } 邮件内容
     */
    private String generateEmailContent(String captcha) {
        Context context = new Context();
        context.setVariable("verifyCode", Arrays.asList(captcha.split("")));
        return templateEngine.process("EmailVerificationCode.html", context);
    }

    /**
     * 发送邮件
     * @param list
     * @param content 邮件内容
     */
    private void sendEmail(List<String> list, String content) {
        MailAccount account = createMailAccount();
        try {
            Mail.create(account)
                    .setTos(list.toArray(new String[0]))
                    .setTitle(MessageConstant.EMAIL_VERIFICATION_CODE)
                    .setContent(content)
                    .setHtml(true)
                    .setUseGlobalSession(false)
                    .send();
        } catch (Exception e) { // 捕获更广泛的异常
            log.info("邮件发送失败：{}", e.getMessage());
            throw new EmailErrorException(MessageConstant.EMAIL_SENDING_EXCEPTION);
        }
    }

    /**
     * 创建邮件账户
     * @return {@link MailAccount } 邮件账户
     */
    private MailAccount createMailAccount() {
        MailAccount account = new MailAccount();
        account.setAuth(true);
        account.setHost(emailProperties.getHost());
        account.setPort(emailProperties.getPort());
        account.setFrom(emailProperties.getFrom());
        account.setUser(emailProperties.getUser());
        account.setPass(emailProperties.getPassword());
        account.setSslEnable(true);
        account.setStarttlsEnable(true);
        return account;
    }

}

