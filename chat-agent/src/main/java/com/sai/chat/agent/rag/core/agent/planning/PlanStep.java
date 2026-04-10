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

package com.sai.chat.agent.rag.core.agent.planning;

import com.sai.chat.agent.rag.core.agent.state.ToolCallResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 执行计划中的单个步骤
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanStep {

    /**
     * 步骤序号（从1开始）
     */
    private int stepId;

    /**
     * 步骤名称
     */
    private String name;

    /**
     * 步骤描述
     */
    private String description;

    /**
     * 步骤类型
     */
    private StepType type;

    /**
     * 工具 ID（当 type 为 TOOL_CALL 时）
     */
    private String toolId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具参数（可能引用前置步骤的输出）
     */
    private Map<String, Object> parameters;

    /**
     * 参数模板（支持变量插值，如 "${step1.output.city}"）
     */
    private String parameterTemplate;

    /**
     * 依赖的前置步骤 ID
     */
    private List<Integer> dependsOn;

    /**
     * 是否可并行执行
     */
    @Builder.Default
    private boolean parallelizable = false;

    /**
     * 并行组 ID（相同 groupId 的步骤可以并行执行）
     */
    private String parallelGroup;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private int maxRetries = 1;

    /**
     * 当前重试次数
     */
    @Builder.Default
    private int currentRetries = 0;

    /**
     * 超时时间（毫秒）
     */
    @Builder.Default
    private long timeoutMs = 60000;

    /**
     * 步骤状态
     */
    @Builder.Default
    private StepStatus status = StepStatus.PENDING;

    /**
     * 执行结果（完成后填充）
     */
    private ToolCallResult result;

    /**
     * 错误信息（失败时填充）
     */
    private String errorMessage;

    /**
     * 开始执行时间（毫秒时间戳）
     */
    private long startTimeMs;

    /**
     * 结束时间（毫秒时间戳）
     */
    private long endTimeMs;

    /**
     * 是否可以执行（依赖已满足）
     */
    public boolean isReady(List<PlanStep> allSteps) {
        if (status != StepStatus.PENDING) {
            return false;
        }
        if (dependsOn == null || dependsOn.isEmpty()) {
            return true;
        }
        for (Integer depId : dependsOn) {
            PlanStep dep = findStep(allSteps, depId);
            if (dep == null || dep.getStatus() != StepStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    private PlanStep findStep(List<PlanStep> steps, int stepId) {
        if (steps == null) return null;
        return steps.stream()
                .filter(s -> s.getStepId() == stepId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取执行耗时（毫秒）
     */
    public long getDurationMs() {
        if (startTimeMs <= 0 || endTimeMs <= 0) return 0;
        return endTimeMs - startTimeMs;
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return currentRetries < maxRetries;
    }

    /**
     * 标记为开始执行
     */
    public void markStarted() {
        this.status = StepStatus.RUNNING;
        this.startTimeMs = System.currentTimeMillis();
    }

    /**
     * 标记为完成
     */
    public void markCompleted(ToolCallResult result) {
        this.status = StepStatus.COMPLETED;
        this.result = result;
        this.endTimeMs = System.currentTimeMillis();
    }

    /**
     * 标记为失败
     */
    public void markFailed(String errorMessage) {
        this.status = StepStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTimeMs = System.currentTimeMillis();
    }

    /**
     * 标记为跳过
     */
    public void markSkipped() {
        this.status = StepStatus.SKIPPED;
        this.endTimeMs = System.currentTimeMillis();
    }

    /**
     * 获取输出内容（用于后续步骤引用）
     */
    public String getOutputContent() {
        if (result == null) return null;
        return result.getContent();
    }
}
