package com.mingri.properties;

import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端管理员生成jwt令牌相关配置
     */
    private String secretKey;
    private long expireTime;
    private String tokenName;


}
