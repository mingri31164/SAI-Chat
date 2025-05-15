package com.mingri.service.mail;

/**
 * 邮件服务
 */
public interface MailService {


    /**
     * 发送邮箱验证码
     **/
    void sendEmailCaptcha(String email);




}
