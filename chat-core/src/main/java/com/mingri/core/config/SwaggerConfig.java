package com.mingri.core.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.Data;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig implements WebMvcConfigurer {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("SAI-Chat接口文档")
                .packagesToScan("com.mingri")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI swaggerOpenAPI(){
        return new OpenAPI()
                .info(new Info().title("SAI-Chat接口文档")
                        // 信息
                        .contact(new Contact().name("mingri31164").email("3116430062@qq.com").url("地址"))
                        // 简介
                        .description("SAI-Chat项目API文档")
                        // 版本
                        .version("v1")
                        // 许可证
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("外部文档")
                        .url("https://springshop.wiki.github.org/docs"));
    }

    /**
     * 设置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }


}
