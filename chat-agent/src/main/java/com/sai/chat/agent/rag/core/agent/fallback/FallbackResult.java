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
 * 兜底结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FallbackResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 兜底方式
     */
    private FallbackMethod method;

    /**
     * 结果内容
     */
    private String content;

    /**
     * 原始错误
     */
    private String originalError;

    /**
     * 是否是部分结果
     */
    private boolean partial;

    /**
     * 是否升级处理
     */
    private boolean escalated;

    /**
     * 兜底方式
     */
    public enum FallbackMethod {
        /**
         * 原始执行成功
         */
        ORIGINAL,

        /**
         * 重试后成功
         */
        RETRY,

        /**
         * 切换到备用工具
         */
        BACKUP_TOOL,

        /**
         * 降级到简单模式（RAG Only）
         */
        SIMPLE_MODE,

        /**
         * 使用缓存结果
         */
        CACHED,

        /**
         * 返回部分结果
         */
        PARTIAL,

        /**
         * 人工升级
         */
        HUMAN_ESCALATION,

        /**
         * 完全失败
         */
        FAILED
    }

    /**
     * 创建成功结果
     */
    public static FallbackResult success(String content) {
        return FallbackResult.builder()
                .success(true)
                .method(FallbackMethod.ORIGINAL)
                .content(content)
                .build();
    }

    /**
     * 创建重试成功结果
     */
    public static FallbackResult retrySuccess(String content) {
        return FallbackResult.builder()
                .success(true)
                .method(FallbackMethod.RETRY)
                .content(content)
                .build();
    }

    /**
     * 创建降级结果
     */
    public static FallbackResult degraded(String content, String originalError) {
        return FallbackResult.builder()
                .success(true)
                .method(FallbackMethod.SIMPLE_MODE)
                .content(content)
                .originalError(originalError)
                .partial(true)
                .build();
    }

    /**
     * 创建部分结果
     */
    public static FallbackResult partial(String content, String originalError) {
        return FallbackResult.builder()
                .success(true)
                .method(FallbackMethod.PARTIAL)
                .content(content)
                .originalError(originalError)
                .partial(true)
                .build();
    }

    /**
     * 创建升级结果
     */
    public static FallbackResult escalated(String message) {
        return FallbackResult.builder()
                .success(false)
                .method(FallbackMethod.HUMAN_ESCALATION)
                .content(message)
                .escalated(true)
                .build();
    }

    /**
     * 创建完全失败结果
     */
    public static FallbackResult failed(String errorMessage) {
        return FallbackResult.builder()
                .success(false)
                .method(FallbackMethod.FAILED)
                .content("处理失败: " + errorMessage)
                .originalError(errorMessage)
                .build();
    }
}
