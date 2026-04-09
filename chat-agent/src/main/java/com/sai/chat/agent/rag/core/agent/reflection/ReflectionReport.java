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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 反思报告
 * <p>
 * 封装对 Agent 执行过程的深入分析报告，包括评估结果、失败原因、改进建议等。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReflectionReport {

    /**
     * 评估状态
     */
    private ReflectionStatus status;

    /**
     * 置信度 [0, 1]
     */
    private double confidence;

    /**
     * 评估理由
     */
    private String reasoning;

    /**
     * 关键观察列表
     */
    private List<String> observations;

    /**
     * 失败原因（如果有）
     */
    private String failureReason;

    /**
     * 是否需要策略调整
     */
    @Builder.Default
    private boolean needsAdjustment = false;

    /**
     * 改进建议
     */
    private String improvementSuggestion;

    /**
     * 建议的重试次数
     */
    @Builder.Default
    private int suggestedRetries = 0;

    /**
     * 判断是否需要继续执行
     */
    public boolean shouldContinue() {
        return status == ReflectionStatus.SUCCESS || 
               (status == ReflectionStatus.PARTIAL && confidence > 0.7);
    }

    /**
     * 判断是否应该回退到简单模式
     */
    public boolean shouldFallbackToSimpleMode() {
        return status == ReflectionStatus.FAILURE && suggestedRetries >= 3;
    }
}
