package com.mingri.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * 管理端管理员生成jwt令牌相关配置
     */
    private String secretKey;
    private long expireTime;
    private String tokenName;


}
