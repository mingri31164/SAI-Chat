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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 单个用例的评测结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvalResult {

    /**
     * 用例ID
     */
    private String caseId;

    /**
     * 用例名称
     */
    private String caseName;

    /**
     * 是否通过
     */
    private boolean passed;

    /**
     * 综合得分(0-100)
     */
    private double score;

    /**
     * 各维度得分
     */
    @Builder.Default
    private Map<String, Double> dimensionScores = Map.of();

    /**
     * 实际回答
     */
    private String actualAnswer;

    /**
     * 执行耗时(ms)
     */
    private long durationMs;

    /**
     * 迭代次数
     */
    private int iterations;

    /**
     * 工具调用记录
     */
    @Builder.Default
    private List<String> toolCalls = new ArrayList<>();

    /**
     * 匹配详情
     */
    private MatchDetails matchDetails;

    /**
     * 错误信息(如果有)
     */
    private String errorMessage;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 详细评估报告
     */
    @Builder.Default
    private List<String> evaluationNotes = new ArrayList<>();

    /**
     * 用例标签
     */
    private List<String> tags;

    /**
     * 添加评估笔记
     */
    public void addNote(String note) {
        if (this.evaluationNotes == null) {
            this.evaluationNotes = new ArrayList<>();
        }
        this.evaluationNotes.add(note);
    }

    /**
     * 匹配详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchDetails {
        /**
         * 关键词匹配数
         */
        private int keywordMatched;

        /**
         * 关键词总数
         */
        private int keywordTotal;

        /**
         * 关键词覆盖率
         */
        private double keywordCoverage;

        /**
         * 模式匹配结果
         */
        private boolean patternMatched;

        /**
         * 工具调用匹配数
         */
        private int toolCallsMatched;

        /**
         * 工具调用总数
         */
        private int toolCallsTotal;

        /**
         * 是否在期望时间内完成
         */
        private boolean withinTimeLimit;

        /**
         * 是否在期望迭代次数内完成
         */
        private boolean withinIterationLimit;
    }
}
