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

import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;

/**
 * Agent 执行器接口
 * <p>
 * 定义 Agent 核心执行循环的契约，支持同步和流式两种执行方式。
 * <p>
 * Agent 循环状态机：
 * <pre>
 * ┌─────────┐
 * │THINKING │ ──► LLM 推理下一步行动
 * └────┬────┘
 *      │
 *      ▼
 * ┌─────────┐
 * │PLANNING │ ──► 决定调用工具或直接回答
 * └────┬────┘
 *      │
 *      ├──────► [调用工具] ──► EXECUTING
 *      │
 *      └──────► [直接回答] ──► COMPLETED
 *                  │
 *                  ▼
 *           ┌─────────┐
 *           │OBSERVING│ ──► 评估工具结果是否满足目标
 *           └────┬────┘
 *                │
 *                ├──── 满足 ──► COMPLETED
 *                │
 *                ├──── 不满足 + 可反思 ──► REFLECTING
 *                │
 *                └──── 不满足 + 不可反思 ──► EXCEEDED
 *                          │
 *                          ▼
 *                   ┌─────────┐
 *                   │REFLECTING│ ──► 分析失败原因,调整策略
 *                   └────┬────┘
 *                        │
 *                        ▼
 *                  ┌─────────┐
 *                  │PLANNING │ ──► 重新规划
 *                  └─────────┘
 * </pre>
 */
public interface AgentExecutor {

    /**
     * 同步执行 Agent
     *
     * @param request Agent 执行请求
     * @return Agent 执行响应
     */
    AgentResponse execute(AgentRequest request);

    /**
     * 流式执行 Agent，通过回调实时输出推理过程
     *
     * @param request  Agent 执行请求
     * @param callback 状态回调
     * @return Agent 执行响应
     */
    AgentResponse executeStream(AgentRequest request, AgentCallback callback);

    /**
     * 获取 Agent 名称
     *
     * @return Agent 名称
     */
    String getName();

    /**
     * 获取 Agent 描述
     *
     * @return Agent 描述
     */
    String getDescription();
}
