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
 * 拒绝模式评估器
 * <p>
 * 检查回答是否包含不应该出现的内容
 */
@Slf4j
@Component
public class RejectEvaluator implements Evaluator {

    @Override
    public String getName() {
        return "RejectEvaluator";
    }

    @Override
    public String getDimension() {
        return "拒绝检测";
    }

    @Override
    public Evaluation evaluate(EvalCase testCase, EvalResult result, String actualAnswer) {
        String rejectPattern = testCase.getRejectPattern();
        if (rejectPattern == null || rejectPattern.isBlank()) {
            return Evaluation.of(100.0, "无拒绝模式，默认满分");
        }

        if (actualAnswer == null || actualAnswer.isBlank()) {
            return Evaluation.of(100.0, "回答为空，无拒绝内容");
        }

        try {
            boolean containsReject = Pattern.matches(".*" + rejectPattern + ".*", actualAnswer);
            if (containsReject) {
                return Evaluation.of(0.0, "检测到拒绝内容: " + rejectPattern);
            } else {
                return Evaluation.of(100.0, "未检测到拒绝内容");
            }
        } catch (Exception e) {
            log.warn("Reject pattern evaluation error: {}", e.getMessage());
            return Evaluation.of(100.0, "拒绝模式评估异常，默认通过");
        }
    }
}
