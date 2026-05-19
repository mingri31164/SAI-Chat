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
 * Agent 状态枚举
 * <p>
 * 描述 Agent 执行循环中的各个状态节点：
 * <ul>
 *   <li>{@link #IDLE} - 空闲状态，未开始执行</li>
 *   <li>{@link #THINKING} - 思考中，正在进行推理</li>
 *   <li>{@link #PLANNING} - 规划中，正在分解任务</li>
 *   <li>{@link #EXECUTING} - 执行中，正在调用工具</li>
 *   <li>{@link #OBSERVING} - 观察中，正在评估工具执行结果</li>
 *   <li>{@link #REFLECTING} - 反思中，正在分析失败原因</li>
 *   <li>{@link #COMPLETED} - 已完成，任务成功结束</li>
 *   <li>{@link #FAILED} - 失败，任务执行失败</li>
 *   <li>{@link #EXCEEDED} - 超限终止，因达到最大迭代次数/预算而终止</li>
 * </ul>
 */
public enum AgentStatus {

    /**
     * 空闲状态，尚未开始
     */
    IDLE(0, false),

    /**
     * 思考中，LLM 正在进行推理
     */
    THINKING(1, true),

    /**
     * 规划中，正在分解复杂任务
     */
    PLANNING(2, true),

    /**
     * 执行中，正在调用外部工具或 API
     */
    EXECUTING(3, true),

    /**
     * 观察中，正在评估上一步的执行结果
     */
    OBSERVING(4, true),

    /**
     * 反思中，正在分析失败并调整策略
     */
    REFLECTING(5, true),

    /**
     * 等待用户输入，已暂停等待
     */
    WAITING(5, false),

    /**
     * 已完成，任务成功结束
     */
    COMPLETED(6, false),

    /**
     * 失败，任务执行失败
     */
    FAILED(7, false),

    /**
     * 超限终止，因达到最大迭代次数/预算而终止
     */
    EXCEEDED(8, false);

    private final int order;
    private final boolean active;

    AgentStatus(int order, boolean active) {
        this.order = order;
        this.active = active;
    }

    /**
     * 获取状态序号
     */
    public int getOrder() {
        return order;
    }

    /**
     * 是否为活跃状态（需要持续处理的中间状态）
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 是否为终止状态
     */
    public boolean isTerminal() {
        return !active;
    }

    /**
     * 是否为成功终止
     */
    public boolean isSuccess() {
        return this == COMPLETED || this == WAITING;
    }

    /**
     * 是否为失败终止
     */
    public boolean isFailure() {
        return this == FAILED || this == EXCEEDED;
    }
}
