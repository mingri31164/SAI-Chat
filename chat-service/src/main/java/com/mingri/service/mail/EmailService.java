package com.mingri.service.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@Service
@Slf4j
public class EmailService {
    @Resource
    private JavaMailSender mailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    String from;

    public void sendHtmlMessage(String to, String subject, String templateName, Context context) {
        try {
            String process = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(process, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
