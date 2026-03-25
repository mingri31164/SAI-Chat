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

package com.sai.chat.agent.rag.controller;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.framework.web.Results;
import com.sai.chat.agent.rag.core.intent.IntentClassifier;
import com.sai.chat.agent.rag.core.rewrite.QueryRewriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG 核心能力接口（意图分类 + 查询改写）
 * <p>
 * 提供独立的 HTTP 接口，供前端或其他服务调用 RAG 预处理能力
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RAGController {

    private final IntentClassifier intentClassifier;
    private final QueryRewriteService queryRewriteService;

    /**
     * 意图分类接口
     *
     * @param question 用户问题
     * @param topN     返回的最大数量（默认 3）
     * @param minScore 最低置信度阈值（默认 0.35）
     * @return 按置信度降序排列的意图节点列表
     */
    @PostMapping("/intent/classify")
    public Result<?> classifyIntent(
            @RequestParam String question,
            @RequestParam(defaultValue = "3") int topN,
            @RequestParam(defaultValue = "0.35") double minScore) {
        try {
            var nodeScores = intentClassifier.topKAboveThreshold(question, topN, minScore);
            return Results.success(nodeScores);
        } catch (Exception e) {
            log.error("意图分类失败, question={}", question, e);
            return Results.failure();
        }
    }

    /**
     * 查询改写与拆分接口
     *
     * @param question  用户问题
     * @param sessionId 会话 ID（用于获取对话历史上下文）
     * @return 改写后的问题和拆分的子问题列表
     */
    @PostMapping("/query/rewrite")
    public Result<?> rewriteQuery(
            @RequestParam String question,
            @RequestParam(required = false) String sessionId) {
        try {
            var result = queryRewriteService.rewrite(question, sessionId != null ? sessionId : "");
            return Results.success(result);
        } catch (Exception e) {
            log.error("查询改写失败, question={}", question, e);
            return Results.failure();
        }
    }

    /**
     * 规则级查询改写（快速路径，不调 LLM）
     *
     * @param question 用户问题
     * @return 清理后的问题
     */
    @PostMapping("/query/rewrite/rule")
    public Result<?> rewriteQueryByRule(@RequestParam String question) {
        try {
            String cleaned = queryRewriteService.rewriteByRule(question);
            return Results.success(Map.of("rewritten_question", cleaned));
        } catch (Exception e) {
            log.error("规则改写失败, question={}", question, e);
            return Results.failure();
        }
    }
}
