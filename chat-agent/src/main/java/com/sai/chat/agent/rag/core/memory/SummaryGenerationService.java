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

package com.sai.chat.agent.rag.core.memory;

import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.rag.config.MemoryProperties;
import com.sai.chat.agent.rag.core.intent.LLMResponseCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话摘要生成服务
 * <p>
 * 当对话轮数超过阈值时触发，将长对话压缩为简短摘要
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryGenerationService {

    private final LLMService llmService;
    private final ConversationMemoryService memoryService;
    private final MemoryProperties memoryProperties;

    /**
     * 为指定会话生成对话摘要
     *
     * @param sessionId 会话 ID
     * @return 生成的摘要文本
     */
    public String generateSummary(String sessionId) {
        if (!memoryProperties.isSummaryEnabled()) {
            return null;
        }

        try {
            String historyText = memoryService.getHistoryForSummary(
                    sessionId,
                    memoryProperties.getSummaryMaxChars() * 5
            );

            if (historyText.isBlank()) {
                log.debug("会话 {} 历史为空，跳过摘要生成", sessionId);
                return null;
            }

            String prompt = buildSummaryPrompt(historyText);
            String raw = llmService.chat(ChatRequest.builder()
                    .messages(List.of(
                            ChatMessage.user(prompt)
                    ))
                    .temperature(0.1D)
                    .thinking(false)
                    .build());

            String summary = LLMResponseCleaner.stripMarkdownCodeFence(raw);
            if (summary != null) {
                summary = summary.trim();
            }
            if (summary != null && summary.length() > memoryProperties.getSummaryMaxChars() * 2) {
                summary = summary.substring(0, memoryProperties.getSummaryMaxChars() * 2);
            }

            memoryService.saveSummary(sessionId, summary);
            log.info("会话 {} 摘要生成成功, 长度={}", sessionId, summary != null ? summary.length() : 0);

            // 摘要生成后清理旧消息并重置轮数，摘要本身保留供后续使用
            memoryService.clearMessages(sessionId);

            return summary;

        } catch (Exception e) {
            log.error("会话 {} 摘要生成失败", sessionId, e);
            return null;
        }
    }

    private String buildSummaryPrompt(String historyText) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是企业知识库对话摘要助手，负责将多轮对话历史压缩为简洁的摘要。\n\n");
        sb.append("任务说明：\n");
        sb.append("1. 提取关键信息：用户问的核心问题、涉及的系统/主题\n");
        sb.append("2. 保留结论：如果助手给出了具体的答案、流程、联系人等信息，保留\n");
        sb.append("3. 压缩语言：删除重复表达、礼貌用语\n");
        sb.append("4. 字数限制：摘要不超过 ").append(memoryProperties.getSummaryMaxChars()).append(" 个中文字符\n\n");
        sb.append("输出规范：\n");
        sb.append("只输出摘要文本，无其他前缀或说明。\n");
        sb.append("摘要需包含：[主题] + [用户意图] + [关键结论]\n\n");
        sb.append("=== 对话历史 ===\n").append(historyText);
        return sb.toString();
    }
}
