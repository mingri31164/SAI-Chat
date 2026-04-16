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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评测API端点
 */
@RestController
@RequestMapping("/api/eval")
@RequiredArgsConstructor
@Slf4j
public class EvalController {

    private final EvalRunner evalRunner;
    private final DefaultEvalCaseLibrary caseLibrary;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 执行所有默认用例
     */
    @PostMapping("/run")
    public EvalReport runEvaluation(
            @RequestParam(defaultValue = "false") boolean parallel) {
        List<EvalCase> cases = caseLibrary.getAllCases();
        log.info("开始执行评测: cases={}, parallel={}", cases.size(), parallel);
        return evalRunner.runEvaluation(cases, parallel);
    }

    /**
     * 执行指定用例集
     */
    @PostMapping("/run/custom")
    public EvalReport runCustomEvaluation(
            @RequestBody List<EvalCase> cases,
            @RequestParam(defaultValue = "false") boolean parallel) {
        log.info("执行自定义评测: cases={}, parallel={}", cases.size(), parallel);
        return evalRunner.runEvaluation(cases, parallel);
    }

    /**
     * 执行冒烟测试
     */
    @PostMapping("/smoke-test")
    public EvalReport runSmokeTest() {
        List<EvalCase> cases = caseLibrary.getSmokeTestCases();
        log.info("执行冒烟测试: cases={}", cases.size());
        return evalRunner.runEvaluation(cases, false);
    }

    /**
     * 获取用例库统计
     */
    @GetMapping("/library/stats")
    public Map<String, Object> getLibraryStats() {
        List<EvalCase> allCases = caseLibrary.getAllCases();
        long easyCount = allCases.stream()
                .filter(c -> c.getDifficulty() == EvalCase.DifficultyLevel.EASY)
                .count();
        long mediumCount = allCases.stream()
                .filter(c -> c.getDifficulty() == EvalCase.DifficultyLevel.MEDIUM)
                .count();
        long hardCount = allCases.stream()
                .filter(c -> c.getDifficulty() == EvalCase.DifficultyLevel.HARD)
                .count();
        long expertCount = allCases.stream()
                .filter(c -> c.getDifficulty() == EvalCase.DifficultyLevel.EXPERT)
                .count();

        return Map.of(
                "totalCases", allCases.size(),
                "byDifficulty", Map.of(
                        "easy", easyCount,
                        "medium", mediumCount,
                        "hard", hardCount,
                        "expert", expertCount
                ),
                "byCategory", Map.of(
                        "简单问答", caseLibrary.getSimpleQACases().size(),
                        "工具调用", caseLibrary.getToolCallCases().size(),
                        "推理", caseLibrary.getReasoningCases().size(),
                        "拒绝", caseLibrary.getRejectionCases().size(),
                        "RAG", caseLibrary.getRAGCases().size()
                )
        );
    }

    /**
     * 获取用例库详情
     */
    @GetMapping("/library")
    public List<EvalCase> getAllCases() {
        return caseLibrary.getAllCases();
    }

    /**
     * 获取简单问答用例
     */
    @GetMapping("/library/simple-qa")
    public List<EvalCase> getSimpleQACases() {
        return caseLibrary.getSimpleQACases();
    }

    /**
     * 获取工具调用用例
     */
    @GetMapping("/library/tool-call")
    public List<EvalCase> getToolCallCases() {
        return caseLibrary.getToolCallCases();
    }

    /**
     * 获取推理用例
     */
    @GetMapping("/library/reasoning")
    public List<EvalCase> getReasoningCases() {
        return caseLibrary.getReasoningCases();
    }

    /**
     * 导出报告为JSON
     */
    @GetMapping("/report/{reportId}/export")
    public String exportReport(@RequestBody EvalReport report) {
        return gson.toJson(report);
    }
}
