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

package com.sai.chat.agent.rag.core.agent.reflection;

import com.sai.chat.agent.rag.core.agent.state.AgentState;
import com.sai.chat.agent.rag.core.agent.state.ToolCallResult;

/**
 * 反思引擎接口
 * <p>
 * 定义对 Agent 执行过程进行评估和调整的能力。
 */
public interface ReflectionEngine {

    /**
     * 评估工具执行结果
     * <p>
     * 基于规则和启发式方法快速评估结果质量。
     *
     * @param state      Agent 状态
     * @param toolResult 工具执行结果
     * @return 反思报告
     */
    ReflectionReport evaluate(AgentState state, ToolCallResult toolResult);

    /**
     * 使用 LLM 进行深入评估
     * <p>
     * 调用 LLM 对复杂场景进行深入分析。
     *
     * @param state      Agent 状态
     * @param toolResult 工具执行结果
     * @return 反思报告
     */
    ReflectionReport evaluateWithLLM(AgentState state, ToolCallResult toolResult);

    /**
     * 生成策略调整提示
     *
     * @param state  Agent 状态
     * @param report 反思报告
     * @return 调整提示，如果不需要调整则返回 null
     */
    String generateAdjustmentHint(AgentState state, ReflectionReport report);
}
