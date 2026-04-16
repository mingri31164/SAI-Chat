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

/**
 * 性能评估器
 * <p>
 * 检查Agent是否在期望的时间和迭代次数内完成任务
 */
@Slf4j
@Component
public class PerformanceEvaluator implements Evaluator {

    /**
     * 默认时间限制(ms)
     */
    private static final long DEFAULT_MAX_DURATION_MS = 30000;

    /**
     * 默认最大迭代次数
     */
    private static final int DEFAULT_MAX_ITERATIONS = 10;

    @Override
    public String getName() {
        return "PerformanceEvaluator";
    }

    @Override
    public String getDimension() {
        return "性能";
    }

    @Override
    public Evaluation evaluate(EvalCase testCase, EvalResult result, String actualAnswer) {
        long maxDuration = testCase.getMaxDurationMs() != null
                ? testCase.getMaxDurationMs()
                : DEFAULT_MAX_DURATION_MS;
        int maxIterations = testCase.getMaxIterations() != null
                ? testCase.getMaxIterations()
                : DEFAULT_MAX_ITERATIONS;

        long actualDuration = result.getDurationMs();
        int actualIterations = result.getIterations();

        boolean withinTime = actualDuration <= maxDuration;
        boolean withinIterations = actualIterations <= maxIterations;

        double timeScore;
        if (actualDuration <= maxDuration * 0.5) {
            timeScore = 100.0;
        } else if (actualDuration <= maxDuration) {
            timeScore = 80.0;
        } else if (actualDuration <= maxDuration * 2) {
            timeScore = 50.0;
        } else {
            timeScore = 0.0;
        }

        double iterationScore = withinIterations ? 100.0 : 0.0;

        double score = timeScore * 0.6 + iterationScore * 0.4;

        StringBuilder details = new StringBuilder();
        details.append(String.format("耗时: %dms (限制: %dms) - %s, ",
                actualDuration, maxDuration,
                withinTime ? "通过" : "超时"));
        details.append(String.format("迭代: %d次 (限制: %d次) - %s",
                actualIterations, maxIterations,
                withinIterations ? "通过" : "超出"));

        return Evaluation.of(score, details.toString());
    }
}
