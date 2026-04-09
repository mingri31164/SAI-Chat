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

package com.sai.chat.agent.rag.core.agent.reasoning;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReAct 响应解析器
 * <p>
 * 从 LLM 的原始输出中解析出 Thought、Action 和 Action Input。
 * <p>
 * 支持多种格式：
 * <ul>
 *   <li>标准格式：Thought: xxx\nAction: xxx\nAction Input: xxx</li>
 *   <li>JSON 格式：Action Input: {"toolId": "xxx", ...}</li>
 *   <li>Markdown 格式：```json ... ```</li>
 * </ul>
 */
@Slf4j
public class ReActResponseParser {

    private static final Gson GSON = new Gson();

    // 匹配 Thought 行
    private static final Pattern THOUGHT_PATTERN = Pattern.compile(
            "(?i)^\\s*Thought:\\s*(.+?)\\s*$",
            Pattern.MULTILINE
    );

    // 匹配 Action 行
    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "(?i)^\\s*Action:\\s*(.+?)\\s*$",
            Pattern.MULTILINE
    );

    // 匹配 Action Input 行
    private static final Pattern ACTION_INPUT_PATTERN = Pattern.compile(
            "(?i)^\\s*Action\\s*Input:\\s*(\\{.+?\\}|.+?)\\s*$",
            Pattern.MULTILINE | Pattern.DOTALL
    );

    // 提取 JSON 对象（处理代码块）
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
            "```(?:json)?\\s*(\\{.*\\})\\s*```",
            Pattern.DOTALL
    );

    /**
     * 解析 LLM 原始输出
     *
     * @param rawOutput LLM 原始输出
     * @return ReAct 推理结果
     */
    public static ReActReasoning parse(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            return ReActReasoning.failure(rawOutput);
        }

        try {
            // 提取 Thought
            String thought = extractThought(rawOutput);

            // 提取 Action
            String actionStr = extractAction(rawOutput);

            // 提取 Action Input
            String actionInputStr = extractActionInput(rawOutput);

            // 解析 Action 类型和参数
            ReActAction action = parseAction(actionStr, actionInputStr);

            return ReActReasoning.success(thought, action, rawOutput);

        } catch (Exception e) {
            log.warn("ReAct 响应解析失败: {}", e.getMessage());
            return ReActReasoning.failure(rawOutput);
        }
    }

    /**
     * 提取 Thought
     */
    private static String extractThought(String raw) {
        Matcher matcher = THOUGHT_PATTERN.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // 如果没有明确的 Thought 标记，尝试取第一行作为 Thought
        String[] lines = raw.split("\n");
        if (lines.length > 0) {
            return lines[0].trim();
        }
        return raw.trim();
    }

    /**
     * 提取 Action
     */
    private static String extractAction(String raw) {
        Matcher matcher = ACTION_PATTERN.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1).trim().toUpperCase();
        }
        return "UNKNOWN";
    }

    /**
     * 提取 Action Input
     */
    private static String extractActionInput(String raw) {
        Matcher matcher = ACTION_INPUT_PATTERN.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    /**
     * 解析 Action 类型和参数
     */
    private static ReActAction parseAction(String actionStr, String actionInputStr) {
        // 判断动作类型
        if (actionStr.contains("TOOL_CALL") || actionStr.contains("CALL") || actionStr.contains("INVOKE")) {
            return parseToolCallAction(actionInputStr);
        } else if (actionStr.contains("ANSWER") || actionStr.contains("RESPOND") || actionStr.contains("FINAL")) {
            return parseAnswerAction(actionInputStr);
        } else if (actionStr.contains("WAIT_INPUT") || actionStr.contains("ASK") || actionStr.contains("CLARIFY")) {
            return parseWaitInputAction(actionInputStr);
        } else if (actionStr.contains("REFLECT") || actionStr.contains("THINK")) {
            // 继续推理
            return ReActAction.unknown();
        }

        // 尝试从 Action Input 内容推断
        return inferActionFromInput(actionInputStr);
    }

    /**
     * 解析工具调用动作
     */
    private static ReActAction parseToolCallAction(String actionInputStr) {
        if (actionInputStr.isBlank()) {
            return ReActAction.unknown();
        }

        try {
            // 清理可能的代码块标记
            String jsonStr = cleanJsonString(actionInputStr);
            
            // 尝试解析为 JSON
            JsonObject json = parseJsonObject(jsonStr);
            if (json != null) {
                String toolId = json.has("toolId") ? json.get("toolId").getAsString() : null;
                String toolName = json.has("toolName") ? json.get("toolName").getAsString() : null;
                JsonObject paramsObj = json.has("parameters") && json.get("parameters").isJsonObject()
                        ? json.get("parameters").getAsJsonObject()
                        : null;

                Map<String, Object> parameters = new HashMap<>();
                if (paramsObj != null) {
                    for (var entry : paramsObj.entrySet()) {
                        parameters.put(entry.getKey(), jsonElementToObject(entry.getValue()));
                    }
                }

                if (toolId != null) {
                    return ReActAction.toolCall(toolId, toolName, parameters);
                }
            }
        } catch (Exception e) {
            log.debug("工具调用参数解析失败: {}", e.getMessage());
        }

        // 如果无法解析参数，返回无参数的调用
        return ReActAction.toolCall(actionInputStr.trim(), null, Map.of());
    }

    /**
     * 解析答案动作
     */
    private static ReActAction parseAnswerAction(String actionInputStr) {
        if (actionInputStr.isBlank()) {
            return ReActAction.unknown();
        }

        try {
            String jsonStr = cleanJsonString(actionInputStr);
            JsonObject json = parseJsonObject(jsonStr);
            if (json != null && json.has("answer")) {
                String answer = json.get("answer").getAsString();
                return ReActAction.answer(answer);
            }
        } catch (Exception e) {
            log.debug("答案解析失败: {}", e.getMessage());
        }

        // 如果不是 JSON 格式，直接使用原始文本作为答案
        return ReActAction.answer(actionInputStr.trim());
    }

    /**
     * 解析等待用户输入动作
     */
    private static ReActAction parseWaitInputAction(String actionInputStr) {
        if (actionInputStr.isBlank()) {
            return ReActAction.waitInput("请提供更多信息");
        }

        try {
            String jsonStr = cleanJsonString(actionInputStr);
            JsonObject json = parseJsonObject(jsonStr);
            if (json != null && json.has("message")) {
                String message = json.get("message").getAsString();
                return ReActAction.waitInput(message);
            }
        } catch (Exception e) {
            log.debug("等待输入消息解析失败: {}", e.getMessage());
        }

        return ReActAction.waitInput(actionInputStr.trim());
    }

    /**
     * 从 Action Input 内容推断动作类型
     */
    private static ReActAction inferActionFromInput(String actionInputStr) {
        if (actionInputStr.isBlank()) {
            return ReActAction.unknown();
        }

        // 检查是否为 JSON 格式
        String jsonStr = cleanJsonString(actionInputStr);
        if (jsonStr.startsWith("{")) {
            try {
                JsonObject json = parseJsonObject(jsonStr);
                if (json != null) {
                    if (json.has("toolId") || json.has("tool_name")) {
                        return parseToolCallAction(actionInputStr);
                    } else if (json.has("answer")) {
                        return parseAnswerAction(actionInputStr);
                    }
                }
            } catch (Exception e) {
                // 不是有效 JSON
            }
        }

        // 非 JSON 格式，视为最终答案
        return ReActAction.answer(actionInputStr.trim());
    }

    /**
     * 清理 JSON 字符串（去除代码块等）
     */
    private static String cleanJsonString(String input) {
        if (input == null) return "";
        String result = input.trim();

        // 处理 markdown 代码块
        Matcher blockMatcher = JSON_BLOCK_PATTERN.matcher(result);
        if (blockMatcher.find()) {
            result = blockMatcher.group(1);
        }

        return result.trim();
    }

    /**
     * 解析 JSON 对象
     */
    private static JsonObject parseJsonObject(String jsonStr) {
        if (jsonStr == null || !jsonStr.startsWith("{")) {
            return null;
        }
        try {
            return GSON.fromJson(jsonStr, JsonObject.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * 将 JsonElement 转换为 Java 对象
     */
    private static Object jsonElementToObject(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) return primitive.getAsString();
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            if (primitive.isNumber()) return primitive.getAsNumber();
        }
        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return element.toString();
    }
}
