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

package com.sai.chat.agent.rag.controller;

import com.sai.chat.agent.rag.core.agent.AgentCallback;
import com.sai.chat.agent.rag.core.agent.AgentService;
import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import com.sai.chat.agent.rag.core.agent.state.ToolCallRecord;
import com.sai.chat.agent.rag.core.pipeline.SSEventType;
import com.sai.chat.agent.framework.web.SseEmitterSender;
import com.sai.chat.agent.framework.web.Results;
import com.sai.chat.agent.framework.convention.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Agent HTTP 控制器
 * <p>
 * 提供 Agent 的 REST API 接口：
 * <ul>
 *   <li>POST /agent/chat - 同步对话</li>
 *   <li>GET /agent/chat/stream - SSE 流式对话</li>
 *   <li>GET /agent/agents - 获取可用 Agent 列表</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * Agent 同步对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody AgentChatRequest request) {
        log.info("Agent 对话请求, question={}, sessionId={}", 
                truncate(request.getQuestion(), 50), request.getSessionId());

        try {
            AgentRequest agentRequest = buildAgentRequest(request);
            AgentResponse response = agentService.execute(agentRequest);

            if (response.isSuccess()) {
                return Results.success(response);
            } else {
                return buildFailure("AGENT_ERROR", response.getErrorMessage(), response);
            }

        } catch (Exception e) {
            log.error("Agent 对话异常, question={}", truncate(request.getQuestion(), 50), e);
            return buildFailure("AGENT_ERROR", "Agent 执行失败: " + e.getMessage(), null);
        }
    }

    /**
     * Agent SSE 流式对话
     *
     * @param request 流式请求
     * @param emitter  SSE 发射器
     */
    @GetMapping("/chat/stream")
    public SseEmitter streamChat(
            @RequestParam String question,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false, defaultValue = "false") boolean deepThinking,
            @RequestParam(required = false, defaultValue = "10") int maxIterations,
            SseEmitter emitter) {

        log.info("Agent SSE 流式请求, question={}, sessionId={}, deepThinking={}", 
                truncate(question, 50), sessionId, deepThinking);

        // 设置超时时间（0 表示不超时）
        emitter.onCompletion(() -> log.info("SSE 连接完成"));
        emitter.onTimeout(() -> log.warn("SSE 连接超时"));
        emitter.onError(e -> log.error("SSE 连接错误", e));

        SseEmitterSender sender = new SseEmitterSender(emitter);

        // 构建请求
        AgentRequest agentRequest = AgentRequest.builder()
                .sessionId(sessionId != null ? sessionId : java.util.UUID.randomUUID().toString())
                .question(question)
                .deepThinking(deepThinking)
                .maxIterations(maxIterations)
                .build();

        // 创建回调
        AgentSseCallback callback = new AgentSseCallback(sender);

        // 异步执行
        new Thread(() -> {
            try {
                agentService.executeStream(agentRequest, callback);
            } catch (Exception e) {
                log.error("Agent 流式执行异常", e);
                callback.onError("执行异常: " + e.getMessage(), false);
            }
        }, "agent-stream").start();

        return emitter;
    }

    /**
     * 获取可用 Agent 列表
     */
    @GetMapping("/agents")
    public Result<List<Map<String, String>>> getAgents() {
        return Results.success(agentService.getAvailableAgents());
    }

    /**
     * 获取 Agent 状态
     */
    @GetMapping("/status/{sessionId}")
    public Result<AgentStatus> getStatus(@PathVariable String sessionId) {
        // TODO: 实现会话状态查询
        return Results.success(AgentStatus.IDLE);
    }

    // ==================== 内部方法 ====================

    private static <T> Result<T> buildFailure(String code, String message, T data) {
        return new Result<T>()
                .setCode(code)
                .setMessage(message)
                .setData(data);
    }

    private AgentRequest buildAgentRequest(AgentChatRequest request) {
        return AgentRequest.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .question(request.getQuestion())
                .deepThinking(request.isDeepThinking())
                .maxIterations(request.getMaxIterations())
                .maxTokens(request.getMaxTokens())
                .maxBudget(request.getMaxBudget())
                .allowedTools(request.getAllowedTools())
                .excludedTools(request.getExcludedTools())
                .build();
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }

    // ==================== 内部类 ====================

    /**
     * Agent SSE 回调实现
     */
    private static class AgentSseCallback implements AgentCallback {

        private final SseEmitterSender sender;
        private final AtomicBoolean started = new AtomicBoolean(false);

        public AgentSseCallback(SseEmitterSender sender) {
            this.sender = sender;
        }

        @Override
        public void onStatusChange(AgentStatus status, String message) {
            try {
                sender.sendEvent(SSEventType.INTENT, Map.of(
                        "status", status.name(),
                        "message", message
                ));
            } catch (Exception e) {
                log.warn("SSE 状态变更发送失败: {}", e.getMessage());
            }
        }

        @Override
        public void onThought(String thought, int stepIndex) {
            try {
                sender.sendEvent(SSEventType.THINKING, Map.of(
                        "step", stepIndex,
                        "thought", thought
                ));
            } catch (Exception e) {
                log.warn("SSE 思考内容发送失败: {}", e.getMessage());
            }
        }

        @Override
        public void onToolCallStart(ToolCallRecord record) {
            try {
                sender.sendEvent("tool_start", Map.of(
                        "step", record.getStepIndex(),
                        "toolId", record.getToolId(),
                        "toolName", record.getToolName()
                ));
            } catch (Exception e) {
                log.warn("SSE 工具开始发送失败: {}", e.getMessage());
            }
        }

        @Override
        public void onToolCallEnd(ToolCallRecord record) {
            try {
                sender.sendEvent("tool_end", Map.of(
                        "step", record.getStepIndex(),
                        "toolId", record.getToolId(),
                        "toolName", record.getToolName(),
                        "success", record.getResult() != null && record.getResult().isSuccess(),
                        "summary", record.getResult() != null ? record.getResult().getSummary() : ""
                ));
            } catch (Exception e) {
                log.warn("SSE 工具结束发送失败: {}", e.getMessage());
            }
        }

        @Override
        public void onAnswerContent(String content) {
            try {
                sender.sendEvent(SSEventType.ANSWER, Map.of(
                        "content", content
                ));
            } catch (Exception e) {
                log.warn("SSE 答案内容发送失败: {}", e.getMessage());
            }
        }

        @Override
        public void onError(String error, boolean retryable) {
            try {
                sender.sendEvent(SSEventType.ERROR, Map.of(
                        "error", error,
                        "retryable", retryable
                ));
            } catch (Exception e) {
                log.warn("SSE 错误发送失败: {}", e.getMessage());
            }
        }

        @Override
        public void onComplete(boolean success, String answer, int totalSteps, int totalTokens) {
            try {
                sender.sendEvent(SSEventType.DONE, Map.of(
                        "success", success,
                        "answer", answer != null ? answer : "",
                        "totalSteps", totalSteps,
                        "totalTokens", totalTokens
                ));
                sender.complete();
            } catch (Exception e) {
                log.warn("SSE 完成发送失败: {}", e.getMessage());
            }
        }
    }

    // ==================== 请求 DTO ====================

    @lombok.Data
    public static class AgentChatRequest {
        private String sessionId;
        private String userId;
        private String question;
        private boolean deepThinking = false;
        private int maxIterations = 10;
        private int maxTokens = 8000;
        private double maxBudget = 1.0;
        private List<String> allowedTools;
        private List<String> excludedTools;
    }
}
