package com.mingri.service;

/**
 * @Author: mingri
 * @CreateTime: 2024-10-13 15:35
 * @Description: 公共服务接口
 * @Version: 1.0
 */

public interface CommonService {

    /**
     * @Description: 发送邮箱验证码
     * @Author: mingri31164
     * @Date: 2025/1/21 13:01
     **/
    void sendEmailCaptcha(String email);

}
