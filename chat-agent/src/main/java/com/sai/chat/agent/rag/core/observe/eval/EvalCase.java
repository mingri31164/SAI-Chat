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

package com.sai.chat.agent.rag.core.observe.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 评测用例定义
 * <p>
 * 定义单个评测场景的输入、期望输出和评分标准
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvalCase {

    /**
     * 用例ID
     */
    private String id;

    /**
     * 用例名称
     */
    private String name;

    /**
     * 用例描述
     */
    private String description;

    /**
     * 输入问题
     */
    private String input;

    /**
     * 期望回答关键词列表
     */
    @Builder.Default
    private List<String> expectedKeywords = List.of();

    /**
     * 期望回答(精确匹配)
     */
    private String expectedAnswer;

    /**
     * 期望回答模式(正则表达式)
     */
    private String expectedPattern;

    /**
     * 拒绝回答的模式
     */
    private String rejectPattern;

    /**
     * 期望工具调用列表
     */
    @Builder.Default
    private List<String> expectedToolCalls = List.of();

    /**
     * 期望最大迭代次数
     */
    private Integer maxIterations;

    /**
     * 期望最大耗时(ms)
     */
    private Long maxDurationMs;

    /**
     * 用例标签(用于分类统计)
     */
    @Builder.Default
    private List<String> tags = List.of();

    /**
     * 用例元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = Map.of();

    /**
     * 难度等级
     */
    private DifficultyLevel difficulty;

    /**
     * 难度等级枚举
     */
    public enum DifficultyLevel {
        /**
         * 简单 - 直接回答
         */
        EASY,
        /**
         * 中等 - 需要简单推理
         */
        MEDIUM,
        /**
         * 困难 - 需要多步推理或工具调用
         */
        HARD,
        /**
         * 专家 - 需要复杂规划和反思
         */
        EXPERT
    }
}
