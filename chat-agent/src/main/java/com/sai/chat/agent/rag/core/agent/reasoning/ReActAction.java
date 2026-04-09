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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ReAct 动作解析结果
 * <p>
 * 封装 LLM 推理出的下一步动作，包括动作类型、工具选择和参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReActAction {

    /**
     * 动作类型
     */
    private ActionType actionType;

    /**
     * 工具 ID（当 actionType 为 TOOL_CALL 时）
     */
    private String toolId;

    /**
     * 工具名称（当 actionType 为 TOOL_CALL 时）
     */
    private String toolName;

    /**
     * 工具参数（当 actionType 为 TOOL_CALL 时）
     */
    private Map<String, Object> parameters;

    /**
     * 直接回答内容（当 actionType 为 ANSWER 时）
     */
    private String answer;

    /**
     * 无操作等待用户输入（当 actionType 为 WAIT_INPUT 时）
     */
    private String waitMessage;

    /**
     * 动作枚举
     */
    public enum ActionType {
        /**
         * 调用工具
         */
        TOOL_CALL,

        /**
         * 直接给出最终答案
         */
        ANSWER,

        /**
         * 等待用户进一步输入
         */
        WAIT_INPUT,

        /**
         * 未知动作，需要继续推理
         */
        UNKNOWN
    }

    /**
     * 判断是否为工具调用动作
     */
    public boolean isToolCall() {
        return actionType == ActionType.TOOL_CALL;
    }

    /**
     * 判断是否为最终答案动作
     */
    public boolean isAnswer() {
        return actionType == ActionType.ANSWER;
    }

    /**
     * 判断是否为等待用户输入动作
     */
    public boolean isWaitInput() {
        return actionType == ActionType.WAIT_INPUT;
    }

    /**
     * 判断动作是否有效
     */
    public boolean isValid() {
        return actionType != null && actionType != ActionType.UNKNOWN;
    }

    /**
     * 创建工具调用动作
     */
    public static ReActAction toolCall(String toolId, String toolName, Map<String, Object> parameters) {
        return ReActAction.builder()
                .actionType(ActionType.TOOL_CALL)
                .toolId(toolId)
                .toolName(toolName)
                .parameters(parameters)
                .build();
    }

    /**
     * 创建直接回答动作
     */
    public static ReActAction answer(String answer) {
        return ReActAction.builder()
                .actionType(ActionType.ANSWER)
                .answer(answer)
                .build();
    }

    /**
     * 创建等待用户输入动作
     */
    public static ReActAction waitInput(String message) {
        return ReActAction.builder()
                .actionType(ActionType.WAIT_INPUT)
                .waitMessage(message)
                .build();
    }

    /**
     * 创建未知动作
     */
    public static ReActAction unknown() {
        return ReActAction.builder()
                .actionType(ActionType.UNKNOWN)
                .build();
    }
}
