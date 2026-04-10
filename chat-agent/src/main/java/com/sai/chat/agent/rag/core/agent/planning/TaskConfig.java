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

/**
 * 任务执行配置
 *
 * @param parallelism     最大并行度
 * @param stepTimeoutMs   单步超时（毫秒）
 * @param planTimeoutMs    计划总超时（毫秒）
 * @param enableRetry      是否启用重试
 * @param enableParallel   是否启用并行执行
 * @param failFast         失败快速返回（遇到第一个失败就停止）
 */
public record TaskConfig(
        int parallelism,
        long stepTimeoutMs,
        long planTimeoutMs,
        boolean enableRetry,
        boolean enableParallel,
        boolean failFast
) {

    /**
     * 创建默认配置
     */
    public static TaskConfig defaultConfig() {
        return new TaskConfig(
                3,              // 并行度 3
                120_000L,       // 单步超时 2 分钟
                600_000L,       // 计划总超时 10 分钟
                true,           // 启用重试
                true,           // 启用并行
                false           // 不快速失败
        );
    }

    /**
     * 创建快速配置（低并行，减少超时）
     */
    public static TaskConfig fastConfig() {
        return new TaskConfig(
                2,
                60_000L,
                300_000L,
                true,
                true,
                false
        );
    }

    /**
     * 创建严格配置（高可靠性）
     */
    public static TaskConfig strictConfig() {
        return new TaskConfig(
                1,
                180_000L,
                900_000L,
                true,
                false,
                false
        );
    }
}
