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

package com.sai.chat.agent.rag.core.agent.fallback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 失败兜底策略配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FallbackStrategy {

    /**
     * 是否启用兜底
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private int maxRetries = 2;

    /**
     * 重试间隔（毫秒）
     */
    @Builder.Default
    private long retryIntervalMs = 1000;

    /**
     * 是否降级到简单模式
     */
    @Builder.Default
    private boolean fallbackToSimple = true;

    /**
     * 是否启用备用工具
     */
    @Builder.Default
    private boolean enableBackupTool = true;

    /**
     * 是否返回部分结果
     */
    @Builder.Default
    private boolean returnPartialResult = true;

    /**
     * 默认兜底策略
     */
    public static FallbackStrategy defaultStrategy() {
        return FallbackStrategy.builder()
                .enabled(true)
                .maxRetries(2)
                .retryIntervalMs(1000)
                .fallbackToSimple(true)
                .enableBackupTool(true)
                .returnPartialResult(true)
                .build();
    }

    /**
     * 严格兜底策略（少重试，多保守）
     */
    public static FallbackStrategy strictStrategy() {
        return FallbackStrategy.builder()
                .enabled(true)
                .maxRetries(1)
                .retryIntervalMs(500)
                .fallbackToSimple(true)
                .enableBackupTool(false)
                .returnPartialResult(false)
                .build();
    }
}
