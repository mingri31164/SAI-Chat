package com.mingri.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


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
