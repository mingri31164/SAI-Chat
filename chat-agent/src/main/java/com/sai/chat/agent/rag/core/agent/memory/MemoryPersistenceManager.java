/*
 * 记忆持久化管理器
 */

package com.sai.chat.agent.rag.core.agent.memory;

import com.sai.chat.agent.rag.core.agent.memory.MultiLevelMemoryManager.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 记忆持久化管理器
 * 
 * 负责记忆的存储、检索和清理
 */
@Slf4j
@Component
public class MemoryPersistenceManager {

    // 持久化的语义记忆库
    private final ConcurrentHashMap<String, SemanticMemory> userSemanticMemories = new ConcurrentHashMap<>();
    
    // 会话历史存档
    private final ConcurrentHashMap<String, List<EpisodicMemory>> sessionArchives = new ConcurrentHashMap<>();
    
    // 知识索引
    private final ConcurrentHashMap<String, String> knowledgeIndex = new ConcurrentHashMap<>();

    /**
     * 保存语义记忆
     */
    public void saveSemanticMemory(String userId, SemanticMemory memory) {
        userSemanticMemories.put(userId, memory);
        rebuildIndex(memory);
        log.debug("Saved semantic memory for user: {}, entries: {}", userId, memory.size());
    }

    /**
     * 获取用户语义记忆
     */
    public SemanticMemory getSemanticMemory(String userId) {
        return userSemanticMemories.get(userId);
    }

    /**
     * 获取或创建语义记忆
     */
    public SemanticMemory getOrCreateSemanticMemory(String userId) {
        return userSemanticMemories.computeIfAbsent(userId, k -> {
            log.debug("Created new semantic memory for user: {}", userId);
            return SemanticMemory.builder().build();
        });
    }

    /**
     * 存档会话
     */
    public void archiveSession(String userId, EpisodicMemory episodic) {
        sessionArchives.computeIfAbsent(userId, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(episodic);
        
        log.debug("Archived session for user: {}, total sessions: {}", 
                userId, sessionArchives.get(userId).size());
    }

    /**
     * 获取用户历史会话
     */
    public List<EpisodicMemory> getUserSessionHistory(String userId) {
        return sessionArchives.getOrDefault(userId, List.of());
    }

    /**
     * 搜索全局知识
     */
    public List<String> searchGlobalKnowledge(String query) {
        String lowerQuery = query.toLowerCase();
        return knowledgeIndex.values().stream()
                .filter(k -> k.toLowerCase().contains(lowerQuery))
                .distinct()
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * 存储知识
     */
    public void storeKnowledge(String key, String knowledge, KnowledgeType type) {
        knowledgeIndex.put(key, knowledge);
        
        // 同时更新相关用户的语义记忆
        for (SemanticMemory memory : userSemanticMemories.values()) {
            if (memory.search(key).isEmpty()) {
                memory.store(key, knowledge, type);
            }
        }
        
        log.debug("Stored global knowledge: {}", key);
    }

    /**
     * 更新知识置信度
     */
    public void updateKnowledgeConfidence(String key, double delta) {
        for (SemanticMemory memory : userSemanticMemories.values()) {
            memory.updateConfidence(key, delta);
        }
    }

    /**
     * 重建知识索引
     */
    private void rebuildIndex(SemanticMemory memory) {
        for (Map.Entry<String, KnowledgeEntry> entry : memory.getKnowledgeBase().entrySet()) {
            knowledgeIndex.put(entry.getKey(), entry.getValue().getKnowledge());
        }
    }

    /**
     * 清理用户记忆
     */
    public void clearUserMemory(String userId) {
        userSemanticMemories.remove(userId);
        sessionArchives.remove(userId);
        log.info("Cleared all memory for user: {}", userId);
    }

    /**
     * 清理过期会话
     */
    public void cleanupOldSessions(String userId, int keepRecent) {
        List<EpisodicMemory> sessions = sessionArchives.get(userId);
        if (sessions != null && sessions.size() > keepRecent) {
            List<EpisodicMemory> toKeep = sessions.subList(
                    sessions.size() - keepRecent, sessions.size());
            sessionArchives.put(userId, toKeep.stream().collect(Collectors.toList()));
            log.info("Cleaned up old sessions for user: {}, kept: {}", userId, keepRecent);
        }
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
                "totalUsers", userSemanticMemories.size(),
                "totalSessions", sessionArchives.values().stream().mapToInt(List::size).sum(),
                "totalKnowledgeEntries", knowledgeIndex.size(),
                "activeUsers", userSemanticMemories.values().stream()
                        .filter(m -> m.size() > 0)
                        .count()
        );
    }

    /**
     * 导出用户数据
     */
    public Map<String, Object> exportUserData(String userId) {
        SemanticMemory semantic = getSemanticMemory(userId);
        List<EpisodicMemory> sessions = getUserSessionHistory(userId);
        
        return Map.of(
                "userId", userId,
                "semanticMemory", semantic != null ? semantic.getKnowledgeBase().values() : List.of(),
                "sessionHistory", sessions,
                "exportTime", System.currentTimeMillis()
        );
    }
}
