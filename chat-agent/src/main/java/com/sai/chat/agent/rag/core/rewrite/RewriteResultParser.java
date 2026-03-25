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

package com.sai.chat.agent.rag.core.rewrite;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sai.chat.agent.framework.convention.rag.RewriteResult;
import com.sai.chat.agent.rag.core.intent.LLMResponseCleaner;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询改写 LLM 响应解析器
 * <p>
 * 负责将 LLM 返回的 JSON 解析为 {@link RewriteResult}
 */
@Slf4j
public class RewriteResultParser {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private RewriteResultParser() {
    }

    /**
     * 解析 LLM 返回的 JSON 字符串
     *
     * @param rawResponse LLM 原始响应
     * @return RewriteResult，解析失败返回默认结果
     */
    public static RewriteResult parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            log.warn("LLM 返回为空，使用原始问题作为默认值");
            return defaultResult(rawResponse);
        }

        try {
            String cleaned = LLMResponseCleaner.stripMarkdownCodeFence(rawResponse);
            JsonElement root = JSON_PARSER.parse(cleaned);

            if (!root.isJsonObject()) {
                log.warn("LLM 返回了非 JSON 对象: {}", rawResponse);
                return defaultResult(rawResponse);
            }

            JsonObject obj = root.getAsJsonObject();

            String rewrittenQuestion = extractString(obj, "rewritten_question", "");
            if (rewrittenQuestion.isBlank()) {
                log.warn("rewritten_question 字段为空，使用原始问题");
                rewrittenQuestion = "";
            }

            List<String> subQuestions = new ArrayList<>();
            JsonElement sqEl = obj.get("sub_questions");
            if (sqEl != null && sqEl.isJsonArray()) {
                sqEl.getAsJsonArray().forEach(el -> {
                    if (el.isJsonPrimitive()) {
                        String sq = el.getAsString().trim();
                        if (!sq.isBlank()) {
                            subQuestions.add(sq);
                        }
                    }
                });
            }

            if (subQuestions.isEmpty()) {
                subQuestions.add(rewrittenQuestion.isBlank() ? rawResponse : rewrittenQuestion);
            }

            return RewriteResult.builder()
                    .rewrittenQuestion(rewrittenQuestion.isBlank() ? rawResponse : rewrittenQuestion)
                    .subQuestions(subQuestions)
                    .build();

        } catch (Exception e) {
            log.warn("解析查询改写响应失败: {}", rawResponse, e);
            return defaultResult(rawResponse);
        }
    }

    private static String extractString(JsonObject obj, String key, String defaultVal) {
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonPrimitive()) {
            return el.getAsString().trim();
        }
        return defaultVal;
    }

    private static RewriteResult defaultResult(String originalQuestion) {
        return RewriteResult.builder()
                .rewrittenQuestion(originalQuestion != null ? originalQuestion : "")
                .subQuestions(originalQuestion != null ? List.of(originalQuestion) : List.of())
                .build();
    }
}
