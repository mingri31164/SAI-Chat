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

import com.sai.chat.agent.framework.convention.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agent 执行状态模型
 * <p>
 * 完整记录 Agent 执行过程中的所有状态信息，包括：
 * <ul>
 *   <li>基本上下文：会话信息、用户问题</li>
 *   <li>循环状态：当前状态、迭代次数</li>
 *   <li>推理轨迹：思考历史、工具调用记录</li>
 *   <li>上下文管理：对话历史、工作内存</li>
 *   <li>资源控制：Token 消耗、成本追踪</li>
 * </ul>
 * <p>
 * 该类设计为不可变快照，通过 {@link #copy()} 方法创建副本以支持状态回溯。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentState {

    // ==================== 基本上下文 ====================

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 原始用户问题
     */
    private String originalQuestion;

    // ==================== 循环控制 ====================

    /**
     * 当前 Agent 状态
     */
    @Builder.Default
    private AgentStatus status = AgentStatus.IDLE;

    /**
     * 当前迭代次数
     */
    @Builder.Default
    private AtomicInteger currentIteration = new AtomicInteger(0);

    /**
     * 执行模式
     */
    @Builder.Default
    private AgentMode mode = AgentMode.REACT;

    // ==================== 推理轨迹 ====================

    /**
     * 当前思考过程（最后一次推理的 thought）
     */
    private String currentThought;

    /**
     * 完整的思考历史
     */
    @Builder.Default
    private List<String> thoughtHistory = new ArrayList<>();

    /**
     * 观察历史（每次 Action 后的 Observation）
     */
    @Builder.Default
    private List<String> observationHistory = new ArrayList<>();

    /**
     * 最终答案（任务完成时填充）
     */
    private String finalAnswer;

    // ==================== 工具调用 ====================

    /**
     * 上一次工具调用的结果
     */
    private ToolCallResult lastToolResult;

    /**
     * 工具调用轨迹（按执行顺序记录）
     */
    @Builder.Default
    private List<ToolCallRecord> toolCallTrace = new ArrayList<>();

    // ==================== 上下文管理 ====================

    /**
     * 对话历史消息列表
     */
    @Builder.Default
    private List<ChatMessage> conversationHistory = new ArrayList<>();

    /**
     * 工作内存（跨步骤共享的键值对）
     */
    @Builder.Default
    private Map<String, Object> workingMemory = new HashMap<>();

    // ==================== 资源控制 ====================

    /**
     * 开始执行时间戳（毫秒）
     */
    private long startTimeMs;

    /**
     * 最大迭代次数限制
     */
    @Builder.Default
    private int maxIterations = 10;

    /**
     * 最大 Token 数量限制
     */
    @Builder.Default
    private int maxTokens = 8000;

    /**
     * 最大成本限制（元）
     */
    @Builder.Default
    private double maxBudget = 1.0;

    /**
     * 当前累计 Token 消耗
     */
    @Builder.Default
    private AtomicInteger tokensConsumed = new AtomicInteger(0);

    /**
     * 当前累计成本（元）
     */
    @Builder.Default
    private double costConsumed = 0.0;

    // ==================== 元信息 ====================

    /**
     * 扩展元数据
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    // ==================== 便捷方法 ====================

    /**
     * 获取当前迭代次数
     */
    public int getCurrentIterationCount() {
        return currentIteration.get();
    }

    /**
     * 增加迭代次数并返回新值
     */
    public int incrementIteration() {
        return currentIteration.incrementAndGet();
    }

    /**
     * 获取已消耗 Token 数量
     */
    public int getTokensConsumedCount() {
        return tokensConsumed.get();
    }

    /**
     * 增加 Token 消耗
     */
    public void addTokenConsumption(int tokens) {
        tokensConsumed.addAndGet(tokens);
    }

    /**
     * 检查是否超过迭代次数限制
     */
    public boolean isIterationExceeded() {
        return currentIteration.get() >= maxIterations;
    }

    /**
     * 检查是否超过 Token 限制
     */
    public boolean isTokenExceeded() {
        return tokensConsumed.get() >= maxTokens;
    }

    /**
     * 检查是否超过预算
     */
    public boolean isBudgetExceeded() {
        return costConsumed >= maxBudget;
    }

    /**
     * 检查是否应该终止执行
     */
    public boolean shouldTerminate() {
        return isIterationExceeded() || isTokenExceeded() || isBudgetExceeded();
    }

    /**
     * 添加思考到历史
     */
    public void addThought(String thought) {
        if (thought != null && !thought.isBlank()) {
            thoughtHistory.add(thought);
            currentThought = thought;
        }
    }

    /**
     * 添加观察到历史
     */
    public void addObservation(String observation) {
        if (observation != null && !observation.isBlank()) {
            observationHistory.add(observation);
        }
    }

    /**
     * 添加工具调用记录
     */
    public void addToolCallRecord(ToolCallRecord record) {
        if (record != null) {
            toolCallTrace.add(record);
            lastToolResult = record.getResult();
        }
    }

    /**
     * 向工作内存写入数据
     */
    public void putWorkingMemory(String key, Object value) {
        workingMemory.put(key, value);
    }

    /**
     * 从工作内存读取数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getWorkingMemory(String key) {
        return (T) workingMemory.get(key);
    }

    /**
     * 获取执行耗时（毫秒）
     */
    public long getElapsedTimeMs() {
        if (startTimeMs <= 0) {
            return 0;
        }
        return System.currentTimeMillis() - startTimeMs;
    }

    /**
     * 创建状态快照（用于回溯）
     */
    public AgentState copy() {
        AgentState snapshot = new AgentState();
        snapshot.setSessionId(this.sessionId);
        snapshot.setUserId(this.userId);
        snapshot.setOriginalQuestion(this.originalQuestion);
        snapshot.setStatus(this.status);
        snapshot.setCurrentIteration(new AtomicInteger(this.currentIteration.get()));
        snapshot.setMode(this.mode);
        snapshot.setCurrentThought(this.currentThought);
        snapshot.setThoughtHistory(new ArrayList<>(this.thoughtHistory));
        snapshot.setObservationHistory(new ArrayList<>(this.observationHistory));
        snapshot.setFinalAnswer(this.finalAnswer);
        snapshot.setLastToolResult(this.lastToolResult);
        snapshot.setToolCallTrace(new ArrayList<>(this.toolCallTrace));
        snapshot.setConversationHistory(new ArrayList<>(this.conversationHistory));
        snapshot.setWorkingMemory(new HashMap<>(this.workingMemory));
        snapshot.setStartTimeMs(this.startTimeMs);
        snapshot.setMaxIterations(this.maxIterations);
        snapshot.setMaxTokens(this.maxTokens);
        snapshot.setMaxBudget(this.maxBudget);
        snapshot.setTokensConsumed(new AtomicInteger(this.tokensConsumed.get()));
        snapshot.setCostConsumed(this.costConsumed);
        snapshot.setMetadata(new HashMap<>(this.metadata));
        return snapshot;
    }

    /**
     * 重置状态（用于新任务）
     */
    public void reset() {
        this.status = AgentStatus.IDLE;
        this.currentIteration = new AtomicInteger(0);
        this.currentThought = null;
        this.thoughtHistory.clear();
        this.observationHistory.clear();
        this.finalAnswer = null;
        this.lastToolResult = null;
        this.toolCallTrace.clear();
        this.workingMemory.clear();
        this.tokensConsumed = new AtomicInteger(0);
        this.costConsumed = 0.0;
        this.startTimeMs = System.currentTimeMillis();
    }
}
