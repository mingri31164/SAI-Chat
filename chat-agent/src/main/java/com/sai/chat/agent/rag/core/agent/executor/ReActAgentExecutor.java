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

package com.sai.chat.agent.rag.core.agent.executor;

import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.rag.core.agent.AgentCallback;
import com.sai.chat.agent.rag.core.agent.AgentExecutor;
import com.sai.chat.agent.rag.core.agent.reasoning.ReActAction;
import com.sai.chat.agent.rag.core.agent.reasoning.ReActPromptBuilder;
import com.sai.chat.agent.rag.core.agent.reasoning.ReActReasoning;
import com.sai.chat.agent.rag.core.agent.reasoning.ReActResponseParser;
import com.sai.chat.agent.rag.core.agent.reflection.DefaultReflectionEngine;
import com.sai.chat.agent.rag.core.agent.reflection.ReflectionEngine;
import com.sai.chat.agent.rag.core.agent.reflection.ReflectionReport;
import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.agent.state.AgentState;
import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import com.sai.chat.agent.rag.core.agent.state.ToolCallRecord;
import com.sai.chat.agent.rag.core.agent.state.ToolCallResult;
import com.sai.chat.agent.rag.core.mcp.MCPToolDefinition;
import com.sai.chat.agent.rag.core.mcp.MCPToolRegistry;
import com.sai.chat.agent.rag.core.mcp.RemoteMCPToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ReAct Agent 执行器
 * <p>
 * 实现完整的 ReAct (Reasoning + Acting) 循环：
 * <pre>
 * 1. THOUGHT: LLM 推理下一步行动
 * 2. ACTION: 决定调用工具或直接回答
 * 3. OBSERVATION: 获取工具执行结果
 * 4. (循环) → 直到任务完成或达到限制
 * </pre>
 * <p>
 * 该执行器整合了：
 * <ul>
 *   <li>推理引擎 - LLM 驱动的思考过程</li>
 *   <li>反思引擎 - 评估工具结果是否满足目标</li>
 *   <li>MCP 工具系统 - 调用外部工具获取信息</li>
 * </ul>
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class ReActAgentExecutor implements AgentExecutor {

    private static final String AGENT_NAME = "ReActAgent";
    private static final String AGENT_DESCRIPTION = "基于 ReAct (Reasoning + Acting) 模式的智能 Agent，支持工具调用和反思纠错";

    private final LLMService llmService;
    private final MCPToolRegistry mcpToolRegistry;
    private final RemoteMCPToolExecutor mcpToolExecutor;
    private final ReflectionEngine reflectionEngine;

    @Override
    public AgentResponse execute(AgentRequest request) {
        // 初始化 Agent 状态
        AgentState state = initializeState(request);
        long startTime = System.currentTimeMillis();

        log.info("Agent 执行开始, sessionId={}, question={}", 
                request.getSessionId(), truncate(request.getQuestion(), 50));

        try {
            // 执行 ReAct 循环
            executeReActLoop(state, request, null);

            // 构建响应
            return buildSuccessResponse(state, startTime);

        } catch (Exception e) {
            log.error("Agent 执行异常, sessionId={}", request.getSessionId(), e);
            return buildErrorResponse(state, startTime, e.getMessage());
        }
    }

    @Override
    public AgentResponse executeStream(AgentRequest request, AgentCallback callback) {
        // 初始化 Agent 状态
        AgentState state = initializeState(request);
        long startTime = System.currentTimeMillis();

        log.info("Agent 流式执行开始, sessionId={}, question={}",
                request.getSessionId(), truncate(request.getQuestion(), 50));

        try {
            // 执行 ReAct 循环（带回调）
            executeReActLoop(state, request, callback);

            // 通知完成
            if (callback != null) {
                callback.onComplete(
                        state.getStatus().isSuccess(),
                        state.getFinalAnswer(),
                        state.getCurrentIterationCount(),
                        state.getTokensConsumedCount()
                );
            }

            // 构建响应
            return buildSuccessResponse(state, startTime);

        } catch (Exception e) {
            log.error("Agent 流式执行异常, sessionId={}", request.getSessionId(), e);

            if (callback != null) {
                callback.onError(e.getMessage(), true);
                callback.onComplete(false, null, state.getCurrentIterationCount(), 
                        state.getTokensConsumedCount());
            }

            return buildErrorResponse(state, startTime, e.getMessage());
        }
    }

    @Override
    public String getName() {
        return AGENT_NAME;
    }

    @Override
    public String getDescription() {
        return AGENT_DESCRIPTION;
    }

    // ==================== 核心方法 ====================

    /**
     * 初始化 Agent 状态
     */
    private AgentState initializeState(AgentRequest request) {
        return AgentState.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .originalQuestion(request.getQuestion())
                .status(AgentStatus.IDLE)
                .maxIterations(request.getMaxIterations())
                .maxTokens(request.getMaxTokens())
                .maxBudget(request.getMaxBudget())
                .conversationHistory(new ArrayList<>())
                .thoughtHistory(new ArrayList<>())
                .observationHistory(new ArrayList<>())
                .toolCallTrace(new ArrayList<>())
                .workingMemory(new java.util.HashMap<>(8))
                .build();
    }

    /**
     * 执行 ReAct 主循环
     */
    private void executeReActLoop(AgentState state, AgentRequest request, AgentCallback callback) {
        // 获取可用工具
        List<MCPToolDefinition> availableTools = getAvailableTools(request);

        // 获取上下文摘要
        String contextSummary = buildContextSummary(state);

        // 获取初始观察（首次执行为用户问题）
        String observation = "用户问题: " + request.getQuestion();

        // ReAct 循环
        while (!state.shouldTerminate()) {
            // 更新状态
            state.incrementIteration();
            int currentStep = state.getCurrentIterationCount();

            log.debug("ReAct 步骤 {} 开始, sessionId={}", currentStep, state.getSessionId());

            // 状态变更回调
            fireStatusChange(callback, state, AgentStatus.THINKING, 
                    "正在推理步骤 " + currentStep);

            // ===== 阶段 1: THINKING - LLM 推理 =====
            ReActReasoning reasoning = think(state, request, observation, 
                    availableTools, contextSummary, currentStep == 1);

            if (!reasoning.isSuccess()) {
                log.warn("推理解析失败, step={}, sessionId={}", currentStep, state.getSessionId());
                // 如果推理失败，记录并继续（可能需要更多信息）
                state.addThought("推理解析失败，重新尝试...");
                observation = "推理未能产生有效动作，请重新思考。";
                continue;
            }

            // 记录思考
            state.addThought(reasoning.getThought());
            fireThought(callback, reasoning.getThought(), currentStep);

            ReActAction action = reasoning.getAction();

            // ===== 阶段 2: ACTION - 执行动作 =====
            if (action.isToolCall()) {
                // 工具调用
                state.setStatus(AgentStatus.EXECUTING);
                fireStatusChange(callback, state, AgentStatus.EXECUTING, 
                        "调用工具: " + action.getToolId());

                ToolCallRecord record = ToolCallRecord.builder()
                        .stepIndex(currentStep)
                        .toolId(action.getToolId())
                        .toolName(action.getToolName())
                        .parameters(action.getParameters())
                        .reasoning(reasoning.getThought())
                        .timestamp(System.currentTimeMillis())
                        .build();

                fireToolCallStart(callback, record);

                ToolCallResult result = executeTool(state, action);

                record.setResult(result);
                state.addToolCallRecord(record);
                state.addObservation(result.getSummary());

                fireToolCallEnd(callback, record);

                // ===== 阶段 3: OBSERVATION - 评估结果 =====
                state.setStatus(AgentStatus.OBSERVING);
                ReflectionReport reflection = reflectionEngine.evaluate(state, result);

                if (reflection.shouldContinue()) {
                    // 结果满足需求，准备回答
                    observation = "工具执行成功: " + result.getSummary();
                    // 可以继续收集更多信息或直接回答
                } else {
                    // 结果不满足需求，需要反思
                    observation = "工具执行结果: " + result.getSummary();
                    if (reflection.isNeedsAdjustment()) {
                        String hint = reflectionEngine.generateAdjustmentHint(state, reflection);
                        if (hint != null) {
                            observation += "\n反思: " + hint;
                        }
                    }
                }

            } else if (action.isAnswer()) {
                // 直接回答，任务完成
                state.setStatus(AgentStatus.COMPLETED);
                state.setFinalAnswer(action.getAnswer());
                log.info("Agent 任务完成, sessionId={}, iterations={}", 
                        state.getSessionId(), currentStep);
                return;

            } else if (action.isWaitInput()) {
                // 等待用户输入
                state.setStatus(AgentStatus.COMPLETED);
                state.setFinalAnswer(action.getWaitMessage());
                return;

            } else {
                // UNKNOWN 动作，继续循环
                observation = "无法确定下一步行动，请继续思考。";
            }

            // 检查是否超过迭代次数
            if (state.isIterationExceeded()) {
                state.setStatus(AgentStatus.EXCEEDED);
                state.setFinalAnswer(generateExceededAnswer(state));
                log.warn("Agent 达到最大迭代次数, sessionId={}, iterations={}", 
                        state.getSessionId(), currentStep);
                return;
            }
        }

        // 达到限制但未完成任务
        state.setStatus(AgentStatus.EXCEEDED);
        state.setFinalAnswer(generateExceededAnswer(state));
    }

    /**
     * LLM 推理阶段
     */
    private ReActReasoning think(
            AgentState state,
            AgentRequest request,
            String observation,
            List<MCPToolDefinition> tools,
            String contextSummary,
            boolean isFirstStep) {

        List<ChatMessage> messages;

        if (isFirstStep) {
            // 首次推理
            messages = ReActPromptBuilder.buildInitialPrompt(
                    request.getQuestion(),
                    tools,
                    contextSummary
            );
        } else {
            // 后续推理
            messages = ReActPromptBuilder.buildContinuationPrompt(
                    state,
                    observation,
                    request.getQuestion()
            );
        }

        try {
            // 调用 LLM
            ChatRequest llmRequest = ChatRequest.builder()
                    .messages(messages)
                    .temperature(0.7)
                    .thinking(request.isDeepThinking())
                    .enableTools(false)
                    .maxTokens(request.getMaxTokens() / 2)
                    .build();

            String rawOutput = llmService.chat(llmRequest);

            // 更新 Token 消耗（粗略估计）
            int estimatedTokens = estimateTokens(rawOutput);
            state.addTokenConsumption(estimatedTokens);

            // 解析响应
            return ReActResponseParser.parse(rawOutput);

        } catch (Exception e) {
            log.error("LLM 推理失败, sessionId={}", state.getSessionId(), e);
            return ReActReasoning.failure("LLM 调用失败: " + e.getMessage());
        }
    }

    /**
     * 执行工具调用
     */
    private ToolCallResult executeTool(AgentState state, ReActAction action) {
        String toolId = action.getToolId();
        long startTime = System.currentTimeMillis();

        try {
            // 获取工具定义
            Optional<MCPToolDefinition> toolOpt = mcpToolRegistry.getTool(toolId);
            if (toolOpt.isEmpty()) {
                return ToolCallResult.failure(toolId, action.getToolName(), 
                        "工具 [" + toolId + "] 未找到");
            }

            MCPToolDefinition tool = toolOpt.get();
            String serverName = mcpToolRegistry.getServerName(toolId).orElse("default");

            // 调用工具
            String result = mcpToolExecutor.execute(serverName, toolId, action.getParameters());

            long duration = System.currentTimeMillis() - startTime;

            return ToolCallResult.builder()
                    .success(true)
                    .toolId(toolId)
                    .toolName(tool.getName())
                    .content(result)
                    .durationMs(duration)
                    .build();

        } catch (Exception e) {
            log.error("工具执行失败, toolId={}, sessionId={}", toolId, state.getSessionId(), e);
            return ToolCallResult.failure(toolId, action.getToolName(), e.getMessage());
        }
    }

    /**
     * 获取可用工具列表
     */
    private List<MCPToolDefinition> getAvailableTools(AgentRequest request) {
        List<MCPToolDefinition> allTools = mcpToolRegistry.getAllTools();

        if (allTools == null || allTools.isEmpty()) {
            return List.of();
        }

        // 如果没有工具限制，返回所有工具
        if ((request.getAllowedTools() == null || request.getAllowedTools().isEmpty()) 
                && (request.getExcludedTools() == null || request.getExcludedTools().isEmpty())) {
            return allTools;
        }

        // 过滤工具
        return allTools.stream()
                .filter(tool -> {
                    // 检查是否在允许列表中
                    if (request.getAllowedTools() != null && !request.getAllowedTools().isEmpty()) {
                        if (!request.getAllowedTools().contains(tool.getToolId())) {
                            return false;
                        }
                    }
                    // 检查是否在禁止列表中
                    if (request.getExcludedTools() != null && request.getExcludedTools().contains(tool.getToolId())) {
                        return false;
                    }
                    return true;
                })
                .toList();
    }

    /**
     * 构建上下文摘要
     */
    private String buildContextSummary(AgentState state) {
        StringBuilder sb = new StringBuilder();

        // 添加对话历史摘要
        var history = state.getConversationHistory();
        if (history != null && !history.isEmpty()) {
            sb.append("对话历史 (共 ").append(history.size()).append(" 条消息):\n");
            int count = Math.min(4, history.size());
            for (int i = history.size() - count; i < history.size(); i++) {
                var msg = history.get(i);
                sb.append("- ").append(msg.getRole()).append(": ")
                  .append(truncate(msg.getContent(), 100)).append("\n");
            }
        }

        // 添加工作内存
        var workingMemory = state.getWorkingMemory();
        if (workingMemory != null && !workingMemory.isEmpty()) {
            sb.append("\n工作内存:\n");
            for (var entry : workingMemory.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ")
                  .append(truncate(String.valueOf(entry.getValue()), 50)).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 生成超限回答
     */
    private String generateExceededAnswer(AgentState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("抱歉，您的问题比较复杂，我需要在更多时间内处理。\n\n");
        sb.append("我已经收集了以下信息：\n");

        // 汇总工具调用结果
        var trace = state.getToolCallTrace();
        if (trace != null && !trace.isEmpty()) {
            sb.append("已查询的信息：\n");
            for (var record : trace) {
                sb.append("- ").append(record.getToolName()).append(": ");
                if (record.getResult() != null && record.getResult().getContent() != null) {
                    sb.append(truncate(record.getResult().getContent(), 80));
                }
                sb.append("\n");
            }
        }

        sb.append("\n请您提供更具体的问题，或者将问题拆分成几个小问题，我会更好地为您解答。");
        return sb.toString();
    }

    // ==================== 响应构建 ====================

    private AgentResponse buildSuccessResponse(AgentState state, long startTime) {
        return AgentResponse.builder()
                .success(state.getStatus().isSuccess())
                .answer(state.getFinalAnswer())
                .status(state.getStatus())
                .durationMs(System.currentTimeMillis() - startTime)
                .totalIterations(state.getCurrentIterationCount())
                .toolCallCount(state.getToolCallTrace().size())
                .totalTokens(state.getTokensConsumedCount())
                .totalCost(state.getCostConsumed())
                .toolCallTrace(state.getToolCallTrace())
                .thoughtHistory(state.getThoughtHistory())
                .build();
    }

    private AgentResponse buildErrorResponse(AgentState state, long startTime, String error) {
        return AgentResponse.builder()
                .success(false)
                .answer(state.getFinalAnswer())
                .status(state.getStatus())
                .durationMs(System.currentTimeMillis() - startTime)
                .totalIterations(state.getCurrentIterationCount())
                .toolCallCount(state.getToolCallTrace().size())
                .totalTokens(state.getTokensConsumedCount())
                .totalCost(state.getCostConsumed())
                .toolCallTrace(state.getToolCallTrace())
                .thoughtHistory(state.getThoughtHistory())
                .errorMessage(error)
                .build();
    }

    // ==================== 回调方法 ====================

    private void fireStatusChange(AgentCallback callback, AgentState state, 
                                  AgentStatus status, String message) {
        state.setStatus(status);
        if (callback != null) {
            try {
                callback.onStatusChange(status, message);
            } catch (Exception e) {
                log.warn("回调执行失败: {}", e.getMessage());
            }
        }
    }

    private void fireThought(AgentCallback callback, String thought, int step) {
        if (callback != null) {
            try {
                callback.onThought(thought, step);
            } catch (Exception e) {
                log.warn("回调执行失败: {}", e.getMessage());
            }
        }
    }

    private void fireToolCallStart(AgentCallback callback, ToolCallRecord record) {
        if (callback != null) {
            try {
                callback.onToolCallStart(record);
            } catch (Exception e) {
                log.warn("回调执行失败: {}", e.getMessage());
            }
        }
    }

    private void fireToolCallEnd(AgentCallback callback, ToolCallRecord record) {
        if (callback != null) {
            try {
                callback.onToolCallEnd(record);
            } catch (Exception e) {
                log.warn("回调执行失败: {}", e.getMessage());
            }
        }
    }

    // ==================== 工具方法 ====================

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }

    private int estimateTokens(String text) {
        // 粗略估计：中文字符约 1.5 tokens，英文约 4 字符 1 token
        if (text == null || text.isBlank()) return 0;
        int chineseChars = (int) text.chars().filter(c -> c > 0x4E00 && c < 0x9FA5).count();
        int otherChars = text.length() - chineseChars;
        return (int) (chineseChars * 1.5 + otherChars / 4.0);
    }
}
