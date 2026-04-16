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

import java.util.regex.Pattern;

/**
 * 模式匹配评估器
 * <p>
 * 使用正则表达式验证回答格式
 */
@Slf4j
@Component
public class PatternEvaluator implements Evaluator {

    @Override
    public String getName() {
        return "PatternEvaluator";
    }

    @Override
    public String getDimension() {
        return "模式匹配";
    }

    @Override
    public Evaluation evaluate(EvalCase testCase, EvalResult result, String actualAnswer) {
        String pattern = testCase.getExpectedPattern();
        if (pattern == null || pattern.isBlank()) {
            return Evaluation.of(100.0, "无期望模式，默认满分");
        }

        if (actualAnswer == null || actualAnswer.isBlank()) {
            return Evaluation.of(0.0, "回答为空，无法匹配模式");
        }

        try {
            boolean matched = Pattern.matches(pattern, actualAnswer);
            if (matched) {
                return Evaluation.of(100.0, "模式匹配成功");
            } else {
                return Evaluation.of(0.0, "模式不匹配: " + pattern);
            }
        } catch (Exception e) {
            log.warn("Pattern evaluation error: {}", e.getMessage());
            return Evaluation.of(50.0, "模式评估异常: " + e.getMessage());
        }
    }
}
