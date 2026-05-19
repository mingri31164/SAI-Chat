/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sai.chat.agent.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * <p>
 * 配置登录认证拦截、用户上下文写入、体验环境只读模式三个拦截器，
 * 实现统一的认证与上下文管理
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 体验环境只读模式拦截器
     */
    private final DemoModeInterceptor demoModeInterceptor;

    /**
     * 用户上下文拦截器
     */
    private final UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Sa-Token 登录拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
                    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        HttpServletRequest request = attrs.getRequest();
                        // 异步调度请求跳过（SSE 完成回调会触发 asyncDispatch，此时 Sa-Token 上下文已丢失）
                        if (request.getDispatcherType() == DispatcherType.ASYNC) {
                            return;
                        }
                        // 预检请求放行，避免 CORS 被拦截
                        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                            return;
                        }
                    }
                    StpUtil.checkLogin();
                }))
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/api/rag/chat/sse", "/agent/chat/stream", "/error");

        // 体验环境只读模式拦截器
        registry.addInterceptor(demoModeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/api/rag/chat/sse", "/agent/chat/stream", "/error");

        // 用户上下文拦截器
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/api/rag/chat/sse", "/agent/chat/stream", "/error");
    }
}
