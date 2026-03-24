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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.framework.errorcode.BaseErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.Set;

/**
 * 体验环境只读模式拦截器
 * <p>
 * 当 {@link DemoModeProperties#demoMode} 开启时：
 * <ul>
 *   <li>GET 请求（SSE 聊天接口除外）直接放行</li>
 *   <li>SSE 流式接口返回 SSE 格式的拒绝事件</li>
 *   <li>其他写操作（POST/PUT/DELETE）返回 JSON 格式的拒绝响应</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class DemoModeInterceptor implements HandlerInterceptor {

    private static final String REJECT_MESSAGE = "体验环境仅支持查询操作";
    private static final Set<String> SSE_PATHS = Set.of("/rag/v3/chat");

    private final DemoModeProperties demoModeProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (!demoModeProperties.getDemoMode()) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean isSsePath = SSE_PATHS.contains(path);

        if ("GET".equalsIgnoreCase(request.getMethod()) && !isSsePath) {
            return true;
        }

        if (isSsePath) {
            writeSseReject(response);
        } else {
            writeJsonReject(response);
        }
        return false;
    }

    private void writeSseReject(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        PrintWriter writer = response.getWriter();
        writer.write("event: reject\ndata: " + objectMapper.writeValueAsString(
                new SseMessage("response", REJECT_MESSAGE)) + "\n\n");
        writer.write("event: done\ndata: \"[DONE]\"\n\n");
        writer.flush();
    }

    private void writeJsonReject(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = new Result<Void>()
                .setCode(BaseErrorCode.CLIENT_ERROR.code())
                .setMessage(REJECT_MESSAGE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * SSE 消息结构（用于 JSON 序列化）
     */
    record SseMessage(String type, String content) {}
}
