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

package com.sai.chat.agent.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 模块配置属性
 * <p>
 * 通过 {@code rag.*} 前缀在 application.yml 中配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RAGProperties {

    /**
     * 查询改写配置
     */
    private QueryRewriteConfig queryRewrite = new QueryRewriteConfig();

    /**
     * 检索通道配置
     */
    private SearchConfig search = new SearchConfig();

    /**
     * 歧义引导配置
     */
    private GuidanceConfig guidance = new GuidanceConfig();

    @Data
    public static class QueryRewriteConfig {
        /**
         * 是否启用 LLM 重写（关闭后仅使用规则）
         */
        private boolean enabled = true;

        /**
         * 改写时用于承接上下文的最大历史消息数
         */
        private int maxHistoryMessages = 4;

        /**
         * 改写时用于承接上下文的最大字符数
         */
        private int maxHistoryChars = 500;
    }

    @Data
    public static class SearchConfig {
        /**
         * 向量全局检索通道配置
         */
        private ChannelConfig vectorGlobal = new ChannelConfig();

        /**
         * 意图定向检索通道配置
         */
        private ChannelConfig intentDirected = new ChannelConfig();

        @Data
        public static class ChannelConfig {
            /**
             * 是否启用该通道
             */
            private boolean enabled = true;

            /**
             * 意图置信度阈值，低于此值时启用兜底策略
             */
            private double confidenceThreshold = 0.6;

            /**
             * TopK 倍数，扩展召回候选数量
             */
            private int topKMultiplier = 3;

            /**
             * 最低意图分数阈值
             */
            private double minIntentScore = 0.4;
        }
    }

    @Data
    public static class GuidanceConfig {
        /**
         * 是否启用引导式问答
         */
        private boolean enabled = true;

        /**
         * 触发引导的分数比值阈值
         * 例如 0.8 表示第一和第二候选分数比 >= 0.8 时触发引导
         */
        private double ambiguityScoreRatio = 0.8;

        /**
         * 单次最多展示的选项数量
         */
        private int maxOptions = 6;
    }
}
