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

package com.sai.chat.agent.rag.core.agent;

import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import com.sai.chat.agent.rag.core.agent.state.ToolCallRecord;

/**
 * Agent 状态回调接口
 * <p>
 * 用于在 Agent 执行过程中实时接收状态变更和中间结果。
 */
public interface AgentCallback {

    /**
     * 状态变更回调
     *
     * @param status  新状态
     * @param message 状态消息
     */
    default void onStatusChange(AgentStatus status, String message) {
    }

    /**
     * 思考过程回调
     *
     * @param thought  思考内容
     * @param stepIndex 当前步骤索引
     */
    default void onThought(String thought, int stepIndex) {
    }

    /**
     * 工具调用开始回调
     *
     * @param record 工具调用记录
     */
    default void onToolCallStart(ToolCallRecord record) {
    }

    /**
     * 工具调用结束回调
     *
     * @param record 工具调用记录
     */
    default void onToolCallEnd(ToolCallRecord record) {
    }

    /**
     * 最终答案片段回调（用于流式输出）
     *
     * @param content 答案片段
     */
    default void onAnswerContent(String content) {
    }

    /**
     * 推理过程片段回调（ReAct 格式输出，逐字流式）
     *
     * @param content 推理内容片段
     */
    default void onReasoningContent(String content) {
    }

    /**
     * 推理解析完成回调（解析后得到最终结果）
     *
     * @param actionType  动作类型 ANSWER / WAIT_INPUT / TOOL_CALL
     * @param finalAnswer 最终答案（ANSWER 时）
     * @param waitMessage 等待消息（WAIT_INPUT 时）
     * @param thought     思考过程
     */
    default void onParsedResult(String actionType, String finalAnswer, String waitMessage, String thought) {
    }

    /**
     * 错误回调
     *
     * @param error   错误信息
     * @param retryable 是否可重试
     */
    default void onError(String error, boolean retryable) {
    }

    /**
     * 完成回调
     *
     * @param success      是否成功
     * @param status       最终状态
     * @param answer       最终答案
     * @param totalSteps   总步骤数
     * @param totalTokens  总 Token 消耗
     */
    default void onComplete(boolean success, String status, String answer, int totalSteps, int totalTokens) {
    }
}
