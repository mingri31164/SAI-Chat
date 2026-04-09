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

package com.sai.chat.agent.rag.core.agent.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具调用结果模型
 * <p>
 * 封装工具执行后的结果信息，包括成功/失败状态、执行耗时、资源消耗等。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCallResult {

    /**
     * 调用是否成功
     */
    @Builder.Default
    private boolean success = true;

    /**
     * 工具 ID
     */
    private String toolId;

    /**
     * 工具名称（展示用）
     */
    private String toolName;

    /**
     * 工具执行结果内容
     */
    private String content;

    /**
     * 错误信息（失败时填充）
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private long durationMs;

    /**
     * 本次调用消耗的 Token 数
     */
    @Builder.Default
    private int tokensUsed = 0;

    /**
     * 本次调用预估成本（元）
     */
    @Builder.Default
    private double cost = 0.0;

    /**
     * 结构化数据（可选，用于需要解析的工具返回）
     */
    private Map<String, Object> structuredData;

    /**
     * 创建成功结果
     */
    public static ToolCallResult success(String toolId, String toolName, String content) {
        return ToolCallResult.builder()
                .success(true)
                .toolId(toolId)
                .toolName(toolName)
                .content(content)
                .build();
    }

    /**
     * 创建成功结果（带结构化数据）
     */
    public static ToolCallResult success(String toolId, String toolName, String content, Map<String, Object> structuredData) {
        return ToolCallResult.builder()
                .success(true)
                .toolId(toolId)
                .toolName(toolName)
                .content(content)
                .structuredData(structuredData)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ToolCallResult failure(String toolId, String toolName, String errorMessage) {
        return ToolCallResult.builder()
                .success(false)
                .toolId(toolId)
                .toolName(toolName)
                .errorMessage(errorMessage)
                .content(null)
                .build();
    }

    /**
     * 判断结果是否为空
     */
    public boolean isEmpty() {
        return content == null || content.isBlank();
    }

    /**
     * 获取结果摘要（用于日志和追踪）
     */
    public String getSummary() {
        if (!success) {
            return "工具 [" + toolId + "] 执行失败: " + errorMessage;
        }
        if (content == null) {
            return "工具 [" + toolId + "] 返回空结果";
        }
        int len = Math.min(content.length(), 100);
        String truncated = len < content.length() ? content.substring(0, len) + "..." : content;
        return "工具 [" + toolId + "] 返回: " + truncated;
    }
}
