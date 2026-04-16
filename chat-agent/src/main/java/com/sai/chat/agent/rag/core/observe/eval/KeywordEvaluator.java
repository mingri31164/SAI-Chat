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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 关键词匹配评估器
 * <p>
 * 检查回答是否包含期望的关键词
 */
@Slf4j
@Component
public class KeywordEvaluator implements Evaluator {

    @Override
    public String getName() {
        return "KeywordEvaluator";
    }

    @Override
    public String getDimension() {
        return "关键词匹配";
    }

    @Override
    public Evaluation evaluate(EvalCase testCase, EvalResult result, String actualAnswer) {
        List<String> expected = testCase.getExpectedKeywords();
        if (expected == null || expected.isEmpty()) {
            return Evaluation.of(100.0, "无期望关键词，默认满分");
        }

        if (actualAnswer == null || actualAnswer.isBlank()) {
            return Evaluation.of(0.0, "回答为空",
                    List.of(), expected);
        }

        String lowerAnswer = actualAnswer.toLowerCase();
        List<String> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        for (String keyword : expected) {
            if (lowerAnswer.contains(keyword.toLowerCase())) {
                matched.add(keyword);
            } else {
                unmatched.add(keyword);
            }
        }

        double score = (double) matched.size() / expected.size() * 100;
        String details = String.format("匹配 %d/%d 关键词: %s",
                matched.size(), expected.size(),
                unmatched.isEmpty() ? "全部匹配" : "未匹配: " + unmatched);

        return Evaluation.of(score, details, matched, unmatched);
    }
}
