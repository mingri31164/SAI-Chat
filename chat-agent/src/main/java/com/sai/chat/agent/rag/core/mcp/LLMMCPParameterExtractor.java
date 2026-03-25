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

package com.sai.chat.agent.rag.core.mcp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.infra.chat.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 基于 LLM 的 MCP 工具参数提取器
 * <p>
 * 当意图识别出需要调用 MCP 工具时，使用 LLM 从用户问题中提取工具参数。
 * <p>
 * 工作流程：
 * 1. 构造 Prompt（含工具定义和用户问题）
 * 2. 调用 LLM 获取 JSON 参数
 * 3. 解析并返回参数 Map
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LLMMCPParameterExtractor {

    private final LLMService llmService;

    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * 从用户问题中提取指定工具的参数
     *
     * @param tool    工具定义
     * @param question 用户问题
     * @return 参数名→参数值 Map，提取失败返回空 Map
     */
    public Map<String, Object> extractParameters(MCPToolDefinition tool, String question) {
        if (tool == null || tool.getParameters() == null || tool.getParameters().isEmpty()) {
            return Map.of();
        }

        try {
            String prompt = buildExtractPrompt(tool, question);
            String raw = llmService.chat(ChatRequest.builder()
                    .messages(List.of(ChatMessage.user(prompt)))
                    .temperature(0.1D)
                    .thinking(false)
                    .build());

            return parseParameters(raw, tool);

        } catch (Exception e) {
            log.warn("LLM 参数提取失败, tool={}, question={}", tool.getToolId(), question, e);
            return Map.of();
        }
    }

    private String buildExtractPrompt(MCPToolDefinition tool, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个参数提取助手，根据用户问题提取工具调用参数。\n\n");

        sb.append("【工具信息】\n");
        sb.append("工具名称: ").append(tool.getName()).append("\n");
        sb.append("工具描述: ").append(tool.getDescription()).append("\n");
        sb.append("参数定义:\n");

        for (Map.Entry<String, MCPToolDefinition.ParameterDef> entry : tool.getParameters().entrySet()) {
            String pName = entry.getKey();
            MCPToolDefinition.ParameterDef pDef = entry.getValue();
            sb.append("  - ").append(pName)
              .append(" (").append(pDef.getType()).append(")")
              .append(": ").append(pDef.getDescription());
            if (pDef.isRequired()) {
                sb.append(" [必填]");
            }
            if (pDef.getEnumValues() != null && !pDef.getEnumValues().isEmpty()) {
                sb.append("，可选值: ").append(String.join(", ", pDef.getEnumValues()));
            }
            sb.append("\n");
        }

        sb.append("\n【用户问题】\n").append(question).append("\n\n");
        sb.append("请提取上述问题中的参数值，返回 JSON 格式结果（如参数无法确定则省略）。\n");
        sb.append("输出格式示例: {\"city\":\"北京\",\"date\":\"2026-03-25\"}");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseParameters(String raw, MCPToolDefinition tool) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }

        try {
            // 尝试提取 JSON 对象（跳过 markdown 代码块）
            String jsonStr = raw.trim();
            if (jsonStr.startsWith("```")) {
                int firstBrace = jsonStr.indexOf('{');
                int lastBrace = jsonStr.lastIndexOf('}');
                if (firstBrace >= 0 && lastBrace > firstBrace) {
                    jsonStr = jsonStr.substring(firstBrace, lastBrace + 1);
                }
            }

            JsonElement root = JSON_PARSER.parse(jsonStr);
            if (!root.isJsonObject()) {
                return Map.of();
            }

            JsonObject obj = root.getAsJsonObject();
            Map<String, Object> result = new LinkedHashMap<>();

            for (Map.Entry<String, MCPToolDefinition.ParameterDef> entry : tool.getParameters().entrySet()) {
                String pName = entry.getKey();
                if (obj.has(pName)) {
                    JsonElement val = obj.get(pName);
                    if (!val.isJsonNull()) {
                        result.put(pName, jsonElementToObject(val));
                    }
                }
            }

            return result;

        } catch (Exception e) {
            log.warn("解析 LLM 参数提取结果失败: {}", raw, e);
            return Map.of();
        }
    }

    private Object jsonElementToObject(JsonElement el) {
        if (el.isJsonPrimitive()) {
            var prim = el.getAsJsonPrimitive();
            if (prim.isNumber()) return prim.getAsDouble();
            if (prim.isBoolean()) return prim.getAsBoolean();
            return prim.getAsString();
        }
        if (el.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement e : el.getAsJsonArray()) {
                list.add(jsonElementToObject(e));
            }
            return list;
        }
        if (el.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String key : el.getAsJsonObject().keySet()) {
                map.put(key, jsonElementToObject(el.getAsJsonObject().get(key)));
            }
            return map;
        }
        return el.toString();
    }
}
