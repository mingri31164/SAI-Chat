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

import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.framework.convention.rag.RewriteResult;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.rag.config.RAGProperties;
import com.sai.chat.agent.rag.core.memory.ConversationMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 查询改写与拆分服务默认实现
 * <p>
 * 核心流程：
 * 1. 快速路径：规则改写 + 判断是否需要 LLM 增强
 * 2. 增强路径：获取对话历史 → 构造 Prompt → 调用 LLM → 解析结果
 * 3. 兜底路径：解析失败时使用原始问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultQueryRewriteService implements QueryRewriteService {

    private final LLMService llmService;
    private final ConversationMemoryService memoryService;
    private final RAGProperties ragProperties;

    private static final Pattern GREETING_PATTERN = Pattern.compile(
            "^(你好|您好|hi|hello|hey|请问|麻烦|请教|打扰|抱歉)[，。,!！?？\\s]*"
    );
    private static final Pattern ENDING_PATTERN = Pattern.compile(
            "[。,，!！?？\\s]*(谢谢|感谢|麻烦了|拜拜|再见|好的|知道了)[。,，!！?？]*$"
    );
    private static final Pattern PRONOUN_PATTERN = Pattern.compile(
            "(这个|那个|它|她|他|上面|以上|这点|此处|这里)"
    );

    @Override
    public RewriteResult rewrite(String question, String sessionId) {
        if (question == null || question.isBlank()) {
            return RewriteResult.builder()
                    .rewrittenQuestion("")
                    .subQuestions(List.of())
                    .build();
        }

        String trimmed = question.trim();

        if (!ragProperties.getQueryRewrite().isEnabled()) {
            return simpleResult(trimmed);
        }

        // 获取对话历史作为上下文
        List<ChatMessage> history = memoryService.getRecentHistory(
                sessionId,
                ragProperties.getQueryRewrite().getMaxHistoryMessages()
        );

        if (history.isEmpty()) {
            // 无历史时做简单规则改写
            String cleaned = rewriteByRule(trimmed);
            return simpleResult(cleaned);
        }

        // 有历史时调用 LLM 进行深度改写
        return rewriteWithLLM(trimmed, history);
    }

    @Override
    public String rewriteByRule(String question) {
        if (question == null || question.isBlank()) {
            return "";
        }

        String result = question.trim();

        // 删除问候语前缀
        result = GREETING_PATTERN.matcher(result).replaceFirst("");

        // 删除结束语后缀
        result = ENDING_PATTERN.matcher(result).replaceFirst("");

        // 代词标记（不替换，保留供 LLM 进一步处理）
        // 这里仅做基本清理
        result = result.replaceAll("\\s+", " ").trim();

        return result.isEmpty() ? question : result;
    }

    private RewriteResult simpleResult(String question) {
        return RewriteResult.builder()
                .rewrittenQuestion(question)
                .subQuestions(List.of(question))
                .build();
    }

    private RewriteResult rewriteWithLLM(String question, List<ChatMessage> history) {
        String systemPrompt = buildSystemPrompt();
        String historyText = buildHistoryText(history);

        String userPrompt = String.format(
                "## 用户问题\n%s\n\n## 最近一轮对话历史\n%s\n\n请根据上述信息改写用户问题。",
                question,
                historyText
        );

        ChatRequest request = ChatRequest.builder()
                .messages(List.of(
                        ChatMessage.system(systemPrompt),
                        ChatMessage.user(userPrompt)
                ))
                .temperature(0.1D)
                .topP(0.3D)
                .thinking(false)
                .build();

        try {
            String raw = llmService.chat(request);
            return RewriteResultParser.parse(raw);
        } catch (Exception e) {
            log.warn("LLM 查询改写失败，使用规则改写结果", e);
            return simpleResult(rewriteByRule(question));
        }
    }

    private String buildSystemPrompt() {
        return """
                你是企业知识库问答系统的问题处理助手，擅长理解用户模糊提问，并拆分为独立、可检索的子问题。

                处理步骤：
                1. 指代消解：将代词（这个、那个、它、上面）替换为具体实体
                2. 礼貌过滤：删除问候语（你好、请问、麻烦）和结束语（谢谢、再见）
                3. 问题拆分：如果问题包含多个独立的子问题，拆分为多个问题
                4. 语言规范化：修正错别字、口语化表达

                输出格式（只输出 JSON，无其他文字）：
                {
                  "rewritten_question": "指代消解和清理后的问题",
                  "sub_questions": ["子问题1", "子问题2"]
                }
                """;
    }

    private String buildHistoryText(List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        int maxChars = ragProperties.getQueryRewrite().getMaxHistoryChars();

        for (int i = history.size() - 1; i >= 0 && count < 2; i--) {
            ChatMessage msg = history.get(i);
            String line = msg.getRole().name().toLowerCase() + ": " + msg.getContent();
            if (sb.length() + line.length() > maxChars) {
                break;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
            if ("user".equals(msg.getRole().name().toLowerCase())) {
                count++;
            }
        }
        return sb.toString();
    }
}
