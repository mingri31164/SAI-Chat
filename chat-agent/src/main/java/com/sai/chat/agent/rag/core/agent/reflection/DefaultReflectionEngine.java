/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file of you under the Apache License, Version 2.0
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

package com.sai.chat.agent.rag.core.agent.reflection;

import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.rag.core.agent.state.AgentState;
import com.sai.chat.agent.rag.core.agent.state.ToolCallResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认反思引擎实现
 * <p>
 * 基于 LLM 的反思机制，通过分析工具执行结果判断是否需要调整策略。
 * <p>
 * 反思策略：
 * <ul>
 *   <li>工具执行成功 + 结果非空 → SUCCESS</li>
 *   <li>工具执行成功 + 结果为空 → PARTIAL / FAILURE</li>
 *   <li>工具执行失败 → FAILURE</li>
 *   <li>结果包含错误关键词 → FAILURE</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultReflectionEngine implements ReflectionEngine {

    private final LLMService llmService;

    /**
     * 错误关键词列表（检测工具执行是否失败）
     */
    private static final List<String> ERROR_KEYWORDS = List.of(
            "error", "failed", "failure", "exception", "timeout",
            "not found", "not found", "invalid", "denied", "unauthorized",
            "无结果", "失败", "错误", "异常", "超时", "未找到", "无效"
    );

    /**
     * 成功关键词列表（检测工具执行是否成功）
     */
    private static final List<String> SUCCESS_KEYWORDS = List.of(
            "success", "completed", "found", "result", "data",
            "成功", "完成", "找到", "结果", "数据"
    );

    @Override
    public ReflectionReport evaluate(AgentState state, ToolCallResult toolResult) {
        if (toolResult == null) {
            return buildFailureReport("工具结果为空", "无法评估空结果");
        }

        // 检查工具是否成功执行
        if (!toolResult.isSuccess()) {
            return buildFailureReport(
                    "工具执行失败",
                    toolResult.getErrorMessage() != null ? toolResult.getErrorMessage() : "未知错误"
            );
        }

        // 检查结果是否为空
        if (toolResult.isEmpty()) {
            return ReflectionReport.builder()
                    .status(ReflectionStatus.PARTIAL)
                    .confidence(0.5)
                    .reasoning("工具执行成功但返回空结果")
                    .observations(List.of("工具返回内容为空"))
                    .needsAdjustment(true)
                    .suggestedRetries(1)
                    .improvementSuggestion("考虑换一个工具或调整参数重试")
                    .build();
        }

        // 基于关键词分析结果质量
        String content = toolResult.getContent().toLowerCase();
        int errorCount = countKeywords(content, ERROR_KEYWORDS);
        int successCount = countKeywords(content, SUCCESS_KEYWORDS);

        // 计算质量分数
        double qualityScore = calculateQualityScore(content, errorCount, successCount);

        // 生成反思报告
        return generateReport(state, toolResult, qualityScore, errorCount);
    }

    @Override
    public ReflectionReport evaluateWithLLM(AgentState state, ToolCallResult toolResult) {
        // 先进行基础评估
        ReflectionReport basicReport = evaluate(state, toolResult);
        
        if (basicReport.getConfidence() >= 0.9 || basicReport.getConfidence() <= 0.1) {
            // 基础评估置信度很高或很低，直接返回
            return basicReport;
        }

        try {
            // 使用 LLM 进行深入评估
            String prompt = buildReflectionPrompt(state, toolResult);
            String llmResponse = llmService.chat(ChatRequest.builder()
                    .messages(List.of(ChatMessage.user(prompt)))
                    .temperature(0.1)
                    .thinking(false)
                    .build());

            return parseLLMReflection(llmResponse, basicReport);

        } catch (Exception e) {
            log.warn("LLM 反思评估失败，使用基础评估结果: {}", e.getMessage());
            return basicReport;
        }
    }

    @Override
    public String generateAdjustmentHint(AgentState state, ReflectionReport report) {
        if (!report.isNeedsAdjustment()) {
            return null;
        }

        StringBuilder hint = new StringBuilder();

        if (report.getFailureReason() != null) {
            hint.append("失败原因: ").append(report.getFailureReason()).append("\n");
        }

        if (report.getImprovementSuggestion() != null) {
            hint.append("改进建议: ").append(report.getImprovementSuggestion()).append("\n");
        }

        // 基于历史尝试次数生成建议
        int toolCallCount = state.getToolCallTrace().size();
        if (toolCallCount >= 3) {
            hint.append("已尝试 ").append(toolCallCount).append(" 次，建议回退到简单模式。\n");
        }

        return hint.toString();
    }

    /**
     * 构建失败报告
     */
    private ReflectionReport buildFailureReport(String reasoning, String failureReason) {
        return ReflectionReport.builder()
                .status(ReflectionStatus.FAILURE)
                .confidence(0.0)
                .reasoning(reasoning)
                .observations(List.of(failureReason))
                .failureReason(failureReason)
                .needsAdjustment(true)
                .suggestedRetries(0)
                .improvementSuggestion("检查工具参数或尝试其他工具")
                .build();
    }

    /**
     * 计算内容质量分数
     */
    private double calculateQualityScore(String content, int errorCount, int successCount) {
        // 基础分数
        double score = 0.5;

        // 有成功关键词加一分
        if (successCount > 0) {
            score += Math.min(0.3, successCount * 0.1);
        }

        // 有错误关键词扣分
        if (errorCount > 0) {
            score -= Math.min(0.4, errorCount * 0.15);
        }

        // 内容长度适中（100-5000字符）给额外分数
        int len = content.length();
        if (len >= 100 && len <= 5000) {
            score += 0.1;
        } else if (len < 50) {
            score -= 0.2;
        }

        // 限制在 [0, 1] 范围内
        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * 生成反思报告
     */
    private ReflectionReport generateReport(AgentState state, ToolCallResult toolResult,
                                            double qualityScore, int errorCount) {
        ReflectionStatus status;
        boolean needsAdjustment;
        int suggestedRetries;

        if (qualityScore >= 0.7) {
            status = ReflectionStatus.SUCCESS;
            needsAdjustment = false;
            suggestedRetries = 0;
        } else if (qualityScore >= 0.4) {
            status = ReflectionStatus.PARTIAL;
            needsAdjustment = true;
            suggestedRetries = 1;
        } else {
            status = ReflectionStatus.FAILURE;
            needsAdjustment = true;
            suggestedRetries = errorCount > 0 ? 0 : 1;
        }

        return ReflectionReport.builder()
                .status(status)
                .confidence(qualityScore)
                .reasoning(String.format("内容质量分数: %.2f", qualityScore))
                .observations(List.of(
                        "错误关键词出现次数: " + errorCount,
                        "内容长度: " + toolResult.getContent().length() + " 字符"
                ))
                .needsAdjustment(needsAdjustment)
                .suggestedRetries(suggestedRetries)
                .improvementSuggestion(suggestImprovement(qualityScore, errorCount))
                .build();
    }

    /**
     * 建议改进方案
     */
    private String suggestImprovement(double qualityScore, int errorCount) {
        if (errorCount > 0) {
            return "工具执行返回了错误信息，建议检查参数或尝试其他工具";
        }
        if (qualityScore < 0.3) {
            return "结果质量较低，建议重新组织问题或使用不同的工具";
        }
        if (qualityScore < 0.6) {
            return "结果部分有效，可能需要补充更多信息";
        }
        return null;
    }

    /**
     * 统计关键词出现次数
     */
    private int countKeywords(String content, List<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 构建反思 Prompt
     */
    private String buildReflectionPrompt(AgentState state, ToolCallResult toolResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是 Agent 反思助手，请评估以下工具执行结果是否满足用户需求。\n\n");

        sb.append("【用户原始问题】\n");
        sb.append(state.getOriginalQuestion()).append("\n\n");

        sb.append("【工具名称】\n");
        sb.append(toolResult.getToolName()).append("\n\n");

        sb.append("【工具执行结果】\n");
        sb.append(toolResult.getContent()).append("\n\n");

        sb.append("【历史推理轨迹】\n");
        var thoughts = state.getThoughtHistory();
        if (thoughts != null && !thoughts.isEmpty()) {
            for (int i = 0; i < thoughts.size(); i++) {
                sb.append("步骤 ").append(i + 1).append(": ").append(thoughts.get(i)).append("\n");
            }
        }

        sb.append("\n请评估以上信息，返回 JSON 格式的反思报告：\n");
        sb.append("{\n");
        sb.append("  \"status\": \"SUCCESS/PARTIAL/FAILURE\",\n");
        sb.append("  \"confidence\": 0.0-1.0 的数值,\n");
        sb.append("  \"reasoning\": \"评估理由\",\n");
        sb.append("  \"needsAdjustment\": true/false,\n");
        sb.append("  \"improvementSuggestion\": \"改进建议(如果有)\"\n");
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * 解析 LLM 反思结果
     */
    private ReflectionReport parseLLMReflection(String llmResponse, ReflectionReport fallback) {
        try {
            // 简单解析 JSON
            String statusStr = extractJsonValue(llmResponse, "status");
            String confidenceStr = extractJsonValue(llmResponse, "confidence");
            String reasoning = extractJsonValue(llmResponse, "reasoning");
            String needsAdjustmentStr = extractJsonValue(llmResponse, "needsAdjustment");
            String suggestion = extractJsonValue(llmResponse, "improvementSuggestion");

            ReflectionStatus status = parseStatus(statusStr);
            double confidence = confidenceStr != null ? Double.parseDouble(confidenceStr) : fallback.getConfidence();
            boolean needsAdjustment = "true".equalsIgnoreCase(needsAdjustmentStr);

            return ReflectionReport.builder()
                    .status(status)
                    .confidence(confidence)
                    .reasoning(reasoning != null ? reasoning : fallback.getReasoning())
                    .needsAdjustment(needsAdjustment)
                    .improvementSuggestion(suggestion)
                    .observations(fallback.getObservations())
                    .suggestedRetries(fallback.getSuggestedRetries())
                    .build();

        } catch (Exception e) {
            log.warn("解析 LLM 反思结果失败: {}", e.getMessage());
            return fallback;
        }
    }

    /**
     * 解析状态字符串
     */
    private ReflectionStatus parseStatus(String statusStr) {
        if (statusStr == null) return ReflectionStatus.UNCERTAIN;
        statusStr = statusStr.toUpperCase().trim();
        if (statusStr.contains("SUCCESS")) return ReflectionStatus.SUCCESS;
        if (statusStr.contains("PARTIAL")) return ReflectionStatus.PARTIAL;
        if (statusStr.contains("FAIL")) return ReflectionStatus.FAILURE;
        return ReflectionStatus.UNCERTAIN;
    }

    /**
     * 从文本中提取 JSON 值
     */
    private String extractJsonValue(String text, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"?([^,\"}\\]]+)\"?";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                String value = m.group(1).trim();
                // 去除可能的引号
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
