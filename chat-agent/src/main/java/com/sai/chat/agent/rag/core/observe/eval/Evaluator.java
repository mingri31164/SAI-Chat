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

import java.util.List;

/**
 * 评估器接口
 * <p>
 * 定义评估Agent回答质量的各种维度
 */
public interface Evaluator {

    /**
     * 获取评估器名称
     */
    String getName();

    /**
     * 获取评估维度描述
     */
    String getDimension();

    /**
     * 评估回答
     *
     * @param testCase   测试用例
     * @param result     执行结果
     * @param actualAnswer 实际回答
     * @return 维度得分(0-100)
     */
    Evaluation evaluate(EvalCase testCase, EvalResult result, String actualAnswer);

    /**
     * 评估结果
     */
    record Evaluation(
            /**
             * 得分(0-100)
             */
            double score,
            /**
             * 评估详情
             */
            String details,
            /**
             * 匹配关键词列表
             */
            List<String> matchedKeywords,
            /**
             * 未匹配关键词列表
             */
            List<String> unmatchedKeywords
    ) {
        public static Evaluation of(double score, String details) {
            return new Evaluation(score, details, List.of(), List.of());
        }

        public static Evaluation of(double score, String details,
                                   List<String> matched, List<String> unmatched) {
            return new Evaluation(score, details, matched, unmatched);
        }
    }
}
