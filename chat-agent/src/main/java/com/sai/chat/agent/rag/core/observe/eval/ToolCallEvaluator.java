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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 工具调用评估器
 * <p>
 * 检查Agent是否正确调用了期望的工具
 */
@Slf4j
@Component
public class ToolCallEvaluator implements Evaluator {

    @Override
    public String getName() {
        return "ToolCallEvaluator";
    }

    @Override
    public String getDimension() {
        return "工具调用";
    }

    @Override
    public Evaluation evaluate(EvalCase testCase, EvalResult result, String actualAnswer) {
        List<String> expectedTools = testCase.getExpectedToolCalls();
        List<String> actualTools = result.getToolCalls();

        if (expectedTools == null || expectedTools.isEmpty()) {
            return Evaluation.of(100.0, "无期望工具调用，默认满分");
        }

        if (actualTools == null || actualTools.isEmpty()) {
            if (expectedTools.isEmpty()) {
                return Evaluation.of(100.0, "无工具调用，期望也无工具");
            }
            return Evaluation.of(0.0, "未调用任何工具，期望: " + expectedTools,
                    List.of(), expectedTools);
        }

        Set<String> expectedSet = new HashSet<>(expectedTools.stream()
                .map(String::toLowerCase)
                .toList());
        Set<String> actualSet = new HashSet<>(actualTools.stream()
                .map(String::toLowerCase)
                .toList());

        Set<String> matched = new HashSet<>(expectedSet);
        matched.retainAll(actualSet);

        Set<String> unmatched = new HashSet<>(expectedSet);
        unmatched.removeAll(actualSet);

        Set<String> unexpected = new HashSet<>(actualSet);
        unexpected.removeAll(expectedSet);

        double score;
        String details;

        if (unmatched.isEmpty() && unexpected.isEmpty()) {
            score = 100.0;
            details = "工具调用完全匹配: " + matched;
        } else if (unmatched.isEmpty()) {
            score = 80.0;
            details = String.format("期望工具已调用，但有额外调用: %s", unexpected);
        } else {
            double matchRatio = (double) matched.size() / expectedSet.size();
            score = matchRatio * 100;
            details = String.format("工具调用匹配 %d/%d: 未匹配 %s",
                    matched.size(), expectedSet.size(), unmatched);
        }

        return Evaluation.of(score, details,
                matched.stream().toList(),
                unmatched.stream().toList());
    }
}
