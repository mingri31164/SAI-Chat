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

package com.sai.chat.agent.rag.core.agent.response;

import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import com.sai.chat.agent.rag.core.agent.state.ToolCallRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent 执行响应
 * <p>
 * 封装 Agent 执行完成后的完整结果，包括最终答案、执行统计和轨迹信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentResponse {

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 最终答案
     */
    private String answer;

    /**
     * 执行状态
     */
    private AgentStatus status;

    /**
     * 执行耗时（毫秒）
     */
    private long durationMs;

    /**
     * 总迭代次数
     */
    private int totalIterations;

    /**
     * 工具调用总次数
     */
    private int toolCallCount;

    /**
     * 总 Token 消耗
     */
    private int totalTokens;

    /**
     * 总成本（元）
     */
    private double totalCost;

    /**
     * 工具调用轨迹
     */
    private List<ToolCallRecord> toolCallTrace;

    /**
     * 思考历史（最后一次迭代的完整推理过程）
     */
    private List<String> thoughtHistory;

    /**
     * 错误信息（失败时填充）
     */
    private String errorMessage;

    /**
     * 创建成功响应
     */
    public static AgentResponse success(String answer, long durationMs) {
        return AgentResponse.builder()
                .success(true)
                .answer(answer)
                .status(AgentStatus.COMPLETED)
                .durationMs(durationMs)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static AgentResponse failure(String errorMessage, long durationMs) {
        return AgentResponse.builder()
                .success(false)
                .answer(null)
                .status(AgentStatus.FAILED)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .build();
    }

    /**
     * 创建超限终止响应
     */
    public static AgentResponse exceeded(long durationMs) {
        return AgentResponse.builder()
                .success(false)
                .answer(null)
                .status(AgentStatus.EXCEEDED)
                .errorMessage("执行超过限制（最大迭代次数/Token/预算）")
                .durationMs(durationMs)
                .build();
    }
}
