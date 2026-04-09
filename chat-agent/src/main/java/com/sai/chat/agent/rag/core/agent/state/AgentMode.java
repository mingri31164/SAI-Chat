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

/**
 * Agent 执行模式枚举
 * <p>
 * 定义 Agent 的推理和执行策略：
 * <ul>
 *   <li>{@link #REACT} - 推理-行动交替模式（ReAct），每步推理后立即执行，适合动态交互任务</li>
 *   <li>{@link #PLAN_AND_EXECUTE} - 先规划后执行模式，先制定完整计划再按序执行，适合可预测的静态任务</li>
 *   <li>{@link #HYBRID} - 混合模式，根据任务类型自适应选择</li>
 * </ul>
 */
public enum AgentMode {

    /**
     * ReAct (Reasoning + Acting) 模式
     * <p>
     * 每一步：Thought → Action → Observation → ... → Final Answer
     * 适合需要灵活调整策略的复杂交互任务
     */
    REACT,

    /**
     * Plan-and-Execute 模式
     * <p>
     * 第一阶段：制定完整执行计划
     * 第二阶段：按计划顺序执行每个步骤
     * 适合目标明确、步骤可预见的任务
     */
    PLAN_AND_EXECUTE,

    /**
     * 混合模式
     * <p>
     * 根据任务特征自动选择合适的执行策略
     */
    HYBRID
}
