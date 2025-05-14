package com.mingri.service.helper.mail;

import com.mingri.api.constant.MailConstant;
import com.mingri.api.constant.MessageConstant;
import com.mingri.api.exception.EmailErrorException;
import com.mingri.api.properties.EmailProperties;
import com.mingri.service.CommonService;
import com.mingri.core.utils.RedisUtils;
import com.mingri.core.utils.VerifyCodeUtil;
import io.lettuce.core.RedisException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;



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
    private final JavaMailSender mailSender;  // 新增

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
        sendEmail(email, content);  // 改为单个 email 入参
    }

    private void validateEmailProperties() {
        if (emailProperties.getUsername() == null || emailProperties.getPassword() == null ||
                emailProperties.getFrom() == null || emailProperties.getHost() == null ||
                emailProperties.getPort() == null) {
            throw new EmailErrorException(MessageConstant.EMAIL_VERIFICATION_CODE_CONFIGURATION_EXCEPTION);
        }
    }

    private String getCaptcha(String email) {
        String redisKey = MailConstant.CAPTCHA_CODE_KEY_PRE + email;
        Object oldCode = redisUtils.get(redisKey);
        if (oldCode == null) {
            String captcha = VerifyCodeUtil.generateVerifyCode();
            boolean saveResult = redisUtils.set(redisKey, captcha, emailProperties.getExpireTime());
            if (!saveResult) {
                throw new RedisException(MessageConstant.EXCEPTION_VERIFICATION_CODE_SAVE_FAILED);
            }
            return captcha;
        } else {
            boolean expireResult = redisUtils.expire(redisKey, emailProperties.getExpireTime());
            if (!expireResult) {
                throw new RedisException(MessageConstant.RESET_VERIFICATION_CODE_FAILED);
            }
            return String.valueOf(oldCode);
        }
    }

    private String generateEmailContent(String captcha) {
        Context context = new Context();
        context.setVariable("verifyCode", Arrays.asList(captcha.split("")));
        return templateEngine.process("EmailVerificationCode.html", context);
    }

    /**
     * 发送邮件（用 Spring Boot 邮件）
     */
    private void sendEmail(String to, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(MessageConstant.EMAIL_VERIFICATION_CODE);
            helper.setFrom(emailProperties.getFrom());
            helper.setText(content, true); // true = HTML

            mailSender.send(message);
            log.info("邮件发送成功：{}", to);
        } catch (MessagingException e) {
            log.error("邮件发送失败：{}", e.getMessage());
            throw new EmailErrorException(MessageConstant.EMAIL_SENDING_EXCEPTION);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}

