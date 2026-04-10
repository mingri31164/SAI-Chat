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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 执行计划
 * <p>
 * 描述一个复杂任务被分解后的完整执行步骤序列。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    /**
     * 计划唯一标识
     */
    private String planId;

    /**
     * 计划目标描述
     */
    private String objective;

    /**
     * 原始用户问题
     */
    private String originalQuestion;

    /**
     * 执行步骤列表
     */
    private List<PlanStep> steps;

    /**
     * 计划类型
     */
    private PlanType type;

    /**
     * 预估 Token 消耗
     */
    @Builder.Default
    private int estimatedTokens = 0;

    /**
     * 预估耗时（毫秒）
     */
    @Builder.Default
    private long estimatedDurationMs = 0;

    /**
     * 是否可执行
     */
    @Builder.Default
    private boolean executable = true;

    /**
     * 不可执行的原因
     */
    private String notExecutableReason;

    /**
     * 获取步骤数量
     */
    public int getStepCount() {
        return steps != null ? steps.size() : 0;
    }

    /**
     * 获取已完成步骤数
     */
    public long getCompletedStepCount() {
        if (steps == null) return 0;
        return steps.stream().filter(s -> s.getStatus() == StepStatus.COMPLETED).count();
    }

    /**
     * 计算完成进度
     */
    public double getProgress() {
        if (steps == null || steps.isEmpty()) return 0.0;
        return (double) getCompletedStepCount() / getStepCount();
    }

    /**
     * 计划是否全部完成
     */
    public boolean isCompleted() {
        if (steps == null || steps.isEmpty()) return true;
        return steps.stream().allMatch(s -> s.getStatus() == StepStatus.COMPLETED);
    }
}
