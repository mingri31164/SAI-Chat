package com.mingri.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/25 22:59
 * @ClassName: ChatProperties
 * @Version: 1.0
 */

@Data
@Component
@ConfigurationProperties(prefix = "mini-chat")
public class ChatProperties {

    private String password;
    private int limit;
    private String name;
    private int expires;
    private AiConfig doubao;

    @Data
    public static class AiConfig {
        private String apiKey;
        private int countLimit;
        private int lengthLimit;
        private String model;
    }

}
