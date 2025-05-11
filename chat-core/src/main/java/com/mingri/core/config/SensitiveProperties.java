package com.mingri.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "sensitive")
public class SensitiveProperties {

    /**
     * 开启敏感词过滤
     */
    private boolean enable;
}
