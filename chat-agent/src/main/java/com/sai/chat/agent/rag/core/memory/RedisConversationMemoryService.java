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
import com.sai.chat.agent.rag.constant.RAGConstant;
import com.sai.chat.agent.rag.config.MemoryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的对话记忆服务实现
 * <p>
 * 存储结构：
 * - 会话消息列表：sai:memory:messages:{sessionId}
 * - 对话摘要：sai:memory:summary:{sessionId}
 * - 会话轮数：sai:memory:turns:{sessionId}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisConversationMemoryService implements ConversationMemoryService {

    private final StringRedisTemplate redisTemplate;
    private final MemoryProperties memoryProperties;

    private static final String MSG_KEY_PREFIX = "sai:memory:messages:";
    private static final String SUMMARY_KEY_PREFIX = "sai:memory:summary:";
    private static final String TURNS_KEY_PREFIX = "sai:memory:turns:";

    @Override
    public void saveUserMessage(String sessionId, String message) {
        saveMessage(sessionId, message, "user");
        incrementTurns(sessionId);
    }

    @Override
    public void saveAssistantMessage(String sessionId, String message) {
        saveMessage(sessionId, message, "assistant");
    }

    @Override
    public List<ChatMessage> getRecentHistory(String sessionId, int maxTurns) {
        List<ChatMessage> all = getAllMessages(sessionId);
        int totalTurns = all.size() / 2;
        if (totalTurns <= maxTurns) {
            return all;
        }
        int startIndex = (totalTurns - maxTurns) * 2;
        return all.subList(startIndex, all.size());
    }

    @Override
    public String getSummary(String sessionId) {
        try {
            return redisTemplate.opsForValue().get(SUMMARY_KEY_PREFIX + sessionId);
        } catch (Exception e) {
            log.warn("获取对话摘要失败, sessionId={}", sessionId, e);
            return null;
        }
    }

    @Override
    public void saveSummary(String sessionId, String summary) {
        try {
            redisTemplate.opsForValue().set(
                    SUMMARY_KEY_PREFIX + sessionId,
                    summary,
                    memoryProperties.getTtlMinutes(),
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.warn("保存对话摘要失败, sessionId={}", sessionId, e);
        }
    }

    @Override
    public boolean needSummary(String sessionId) {
        try {
            String turnsStr = redisTemplate.opsForValue().get(TURNS_KEY_PREFIX + sessionId);
            if (turnsStr == null) {
                return false;
            }
            int turns = Integer.parseInt(turnsStr);
            return turns >= memoryProperties.getSummaryStartTurns();
        } catch (Exception e) {
            log.warn("检查是否需要摘要失败, sessionId={}", sessionId, e);
            return false;
        }
    }

    @Override
    public String getHistoryForSummary(String sessionId, int maxChars) {
        List<ChatMessage> all = getAllMessages(sessionId);
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : all) {
            String line = msg.getRole().name().toLowerCase() + ": " + msg.getContent();
            if (sb.length() + line.length() > maxChars) {
                break;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    public void clearMessages(String sessionId) {
        try {
            redisTemplate.delete(MSG_KEY_PREFIX + sessionId);
            redisTemplate.delete(TURNS_KEY_PREFIX + sessionId);
            log.debug("会话消息列表和轮数已清除, sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("清除会话消息列表失败, sessionId={}", sessionId, e);
        }
    }

    @Override
    public void clearSession(String sessionId) {
        try {
            redisTemplate.delete(MSG_KEY_PREFIX + sessionId);
            redisTemplate.delete(SUMMARY_KEY_PREFIX + sessionId);
            redisTemplate.delete(TURNS_KEY_PREFIX + sessionId);
            log.debug("会话记忆已清除, sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("清除会话记忆失败, sessionId={}", sessionId, e);
        }
    }

    private void saveMessage(String sessionId, String message, String role) {
        try {
            String key = MSG_KEY_PREFIX + sessionId;
            redisTemplate.opsForList().rightPush(key, role + "|" + message);
            redisTemplate.expire(key, memoryProperties.getTtlMinutes(), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("保存对话消息失败, sessionId={}, role={}", sessionId, role, e);
        }
    }

    private List<ChatMessage> getAllMessages(String sessionId) {
        try {
            String key = MSG_KEY_PREFIX + sessionId;
            List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
            if (raw == null || raw.isEmpty()) {
                return List.of();
            }
            List<ChatMessage> messages = new ArrayList<>();
            for (String item : raw) {
                int sep = item.indexOf('|');
                if (sep < 0) continue;
                String role = item.substring(0, sep);
                String content = item.substring(sep + 1);
                messages.add(new ChatMessage(ChatMessage.Role.valueOf(role), content));
            }
            return messages;
        } catch (Exception e) {
            log.warn("获取对话历史失败, sessionId={}", sessionId, e);
            return List.of();
        }
    }

    private void incrementTurns(String sessionId) {
        try {
            String key = TURNS_KEY_PREFIX + sessionId;
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, memoryProperties.getTtlMinutes(), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("更新会话轮数失败, sessionId={}", sessionId, e);
        }
    }
}
