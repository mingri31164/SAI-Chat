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

import java.util.List;

/**
 * 对话记忆服务接口
 * <p>
 * 负责管理多轮对话的上下文，支持：
 * - 保存用户和助手的对话消息
 * - 获取最近 N 轮对话历史
 * - 生成对话摘要（长对话压缩）
 */
public interface ConversationMemoryService {

    /**
     * 保存用户消息
     *
     * @param sessionId 会话 ID
     * @param message   消息内容
     */
    void saveUserMessage(String sessionId, String message);

    /**
     * 保存助手回复
     *
     * @param sessionId 会话 ID
     * @param message   回复内容
     */
    void saveAssistantMessage(String sessionId, String message);

    /**
     * 获取最近的对话历史
     *
     * @param sessionId 会话 ID
     * @param maxTurns  最大轮数（每轮包含 user + assistant）
     * @return 对话消息列表
     */
    List<ChatMessage> getRecentHistory(String sessionId, int maxTurns);

    /**
     * 获取对话摘要
     *
     * @param sessionId 会话 ID
     * @return 摘要文本，不存在时返回 null
     */
    String getSummary(String sessionId);

    /**
     * 保存对话摘要
     *
     * @param sessionId 会话 ID
     * @param summary   摘要文本
     */
    void saveSummary(String sessionId, String summary);

    /**
     * 判断当前对话是否需要生成摘要
     *
     * @param sessionId 会话 ID
     * @return true 表示对话轮数已超过阈值，需要摘要
     */
    boolean needSummary(String sessionId);

    /**
     * 获取完整的对话历史（用于生成摘要）
     * <p>
     * 返回格式：[{"role":"user","content":"..."},{"role":"assistant","content":"..."}]
     *
     * @param sessionId 会话 ID
     * @param maxChars 最大字符数限制
     * @return 历史消息文本
     */
    String getHistoryForSummary(String sessionId, int maxChars);

    /**
     * 清除会话记忆
     *
     * @param sessionId 会话 ID
     */
    void clearSession(String sessionId);
}
