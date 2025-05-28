package com.mingri.service.interceptor;

import com.mingri.core.config.UserInfoArgumentResolver;
import com.mingri.service.admin.service.ConversationService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    ConversationService conversationService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserInfoArgumentResolver());
    }


//     使用 addCorsMappings() 无效，而使用 CorsFilter 有效的核心原因是：
//     addCorsMappings() 注册的 CORS 配置 只对 Spring MVC 的 DispatcherServlet 有效，
//     而某些请求（如你提到的 login 接口）可能在被其他 Filter 或拦截器处理时就已经触发 CORS 校验失败了。
//     而CorsFilter可以作用于所有请求（包括 Spring Security、拦截器、预检请求 OPTIONS）
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOriginPatterns("*") // 支持通配但不会违反 allowCredentials 限制
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600);
//    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SignatureInterceptor(conversationService))
                .addPathPatterns("/v1/api/expose/**");
    }
}
