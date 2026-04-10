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

/**
 * 任务执行回调接口
 * <p>
 * 用于实时反馈任务执行状态，支持流式输出和进度跟踪。
 */
public interface TaskCallback {

    /**
     * 步骤开始执行
     */
    default void onStepStart(PlanStep step) {
    }

    /**
     * 步骤执行完成
     */
    default void onStepComplete(PlanStep step, ToolCallResult result) {
    }

    /**
     * 步骤执行失败
     */
    default void onStepFailed(PlanStep step, String errorMessage) {
    }

    /**
     * 步骤重试
     */
    default void onStepRetry(PlanStep step, int retryCount, int maxRetries) {
    }

    /**
     * 计划开始执行
     */
    default void onPlanStart(Plan plan) {
    }

    /**
     * 计划执行完成
     */
    default void onPlanComplete(Plan plan) {
    }

    /**
     * 计划执行失败
     */
    default void onPlanFailed(Plan plan, String reason) {
    }

    /**
     * 进度更新
     */
    default void onProgress(Plan plan, int completedSteps, int totalSteps) {
    }

    /**
     * 空实现
     */
    static TaskCallback noOp() {
        return new TaskCallback() {
        };
    }
}
