/*
 * Agent 多层记忆系统
 * 
 * 实现三种记忆类型:
 * - Working Memory: 当前会话的短期工作记忆
 * - Episodic Memory: 会话历史记忆
 * - Semantic Memory: 持久化知识记忆
 */

package com.sai.chat.agent.rag.core.agent.memory;

import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 多层记忆管理器
 */
public class MultiLevelMemoryManager {

    // ==================== Working Memory ====================
    
    /**
     * 工作记忆 - 当前会话的短期上下文
     */
    @Data
    @Builder
    public static class WorkingMemory {
        private String sessionId;
        private String userId;
        
        @Builder.Default
        private List<MemoryEntry> entries = new ArrayList<>();
        
        @Builder.Default
        private int maxEntries = 50;
        
        private String currentTask;
        private String currentPlan;
        private AgentStatus lastStatus;
        private long lastActiveTime;
        
        public void addEntry(String content, MemoryType type) {
            entries.add(MemoryEntry.builder()
                    .content(content)
                    .type(type)
                    .timestamp(System.currentTimeMillis())
                    .build());
            
            // 清理超出限制的旧条目
            while (entries.size() > maxEntries) {
                entries.remove(0);
            }
        }
        
        public void addEntry(String content, MemoryType type, String metadata) {
            entries.add(MemoryEntry.builder()
                    .content(content)
                    .type(type)
                    .metadata(metadata)
                    .timestamp(System.currentTimeMillis())
                    .build());
            
            while (entries.size() > maxEntries) {
                entries.remove(0);
            }
        }
        
        public List<MemoryEntry> getRecentEntries(int limit) {
            int size = entries.size();
            if (size <= limit) {
                return new ArrayList<>(entries);
            }
            return new ArrayList<>(entries.subList(size - limit, size));
        }
        
        public List<MemoryEntry> getEntriesByType(MemoryType type) {
            return entries.stream()
                    .filter(e -> e.getType() == type)
                    .collect(Collectors.toList());
        }
        
        public String getContextSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("【当前任务】").append(currentTask != null ? currentTask : "无").append("\n");
            sb.append("【当前计划】").append(currentPlan != null ? currentPlan : "无").append("\n");
            sb.append("【最近记忆】").append(entries.size()).append("条\n");
            return sb.toString();
        }
    }
    
    // ==================== Episodic Memory ====================
    
    /**
     * 情景记忆 - 会话历史记录
     */
    @Data
    @Builder
    public static class EpisodicMemory {
        private String sessionId;
        
        @Builder.Default
        private List<Episode> episodes = new ArrayList<>();
        
        @Builder.Default
        private int maxEpisodes = 100;
        
        private String startTime;
        private String endTime;
        private String summary;
        private AgentStatus finalStatus;
        private int totalIterations;
        private long totalDurationMs;
        
        public void addEpisode(Episode episode) {
            episodes.add(episode);
            
            while (episodes.size() > maxEpisodes) {
                episodes.remove(0);
            }
        }
        
        public List<Episode> getEpisodesByOutcome(boolean success) {
            return episodes.stream()
                    .filter(e -> e.isSuccess() == success)
                    .collect(Collectors.toList());
        }
        
        public Episode getLastEpisode() {
            return episodes.isEmpty() ? null : episodes.get(episodes.size() - 1);
        }
        
        public String getLessonLearned() {
            // 分析成功的经验
            List<Episode> successEpisodes = getEpisodesByOutcome(true);
            List<Episode> failEpisodes = getEpisodesByOutcome(false);
            
            StringBuilder sb = new StringBuilder();
            sb.append("【成功经验】").append(successEpisodes.size()).append("次\n");
            sb.append("【失败教训】").append(failEpisodes.size()).append("次\n");
            
            return sb.toString();
        }
    }
    
    /**
     * 情景片段
     */
    @Data
    @Builder
    public static class Episode {
        private String episodeId;
        private int iteration;
        private String task;
        private String action;
        private String observation;
        private boolean success;
        private String reflection;
        private long durationMs;
        private long timestamp;
        
        public String toSummary() {
            return String.format("[迭代%d] %s -> %s (%s)", 
                    iteration, action, success ? "成功" : "失败", 
                    durationMs + "ms");
        }
    }
    
    // ==================== Semantic Memory ====================
    
    /**
     * 语义记忆 - 持久化知识
     */
    @Data
    @Builder
    public static class SemanticMemory {
        
        @Builder.Default
        private ConcurrentHashMap<String, KnowledgeEntry> knowledgeBase = new ConcurrentHashMap<>();
        
        @Builder.Default
        private int maxEntries = 1000;
        
        public void store(String key, String knowledge, KnowledgeType type) {
            KnowledgeEntry entry = KnowledgeEntry.builder()
                    .key(key)
                    .knowledge(knowledge)
                    .type(type)
                    .accessCount(0)
                    .lastAccessTime(System.currentTimeMillis())
                    .createdTime(System.currentTimeMillis())
                    .build();
            
            knowledgeBase.put(key, entry);
            
            // 清理超出限制的旧条目
            if (knowledgeBase.size() > maxEntries) {
                String oldestKey = knowledgeBase.values().stream()
                        .min((a, b) -> Long.compare(a.getLastAccessTime(), b.getLastAccessTime()))
                        .map(KnowledgeEntry::getKey)
                        .orElse(key);
                knowledgeBase.remove(oldestKey);
            }
        }
        
        public void store(String key, String knowledge, KnowledgeType type, double confidence) {
            KnowledgeEntry entry = KnowledgeEntry.builder()
                    .key(key)
                    .knowledge(knowledge)
                    .type(type)
                    .confidence(confidence)
                    .accessCount(0)
                    .lastAccessTime(System.currentTimeMillis())
                    .createdTime(System.currentTimeMillis())
                    .build();
            
            knowledgeBase.put(key, entry);
        }
        
        public KnowledgeEntry retrieve(String key) {
            KnowledgeEntry entry = knowledgeBase.get(key);
            if (entry != null) {
                entry.incrementAccessCount();
                entry.setLastAccessTime(System.currentTimeMillis());
            }
            return entry;
        }
        
        public List<KnowledgeEntry> search(String query) {
            String lowerQuery = query.toLowerCase();
            return knowledgeBase.values().stream()
                    .filter(e -> e.getKnowledge().toLowerCase().contains(lowerQuery))
                    .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
                    .collect(Collectors.toList());
        }
        
        public List<KnowledgeEntry> getByType(KnowledgeType type) {
            return knowledgeBase.values().stream()
                    .filter(e -> e.getType() == type)
                    .collect(Collectors.toList());
        }
        
        public void updateConfidence(String key, double delta) {
            KnowledgeEntry entry = knowledgeBase.get(key);
            if (entry != null) {
                entry.setConfidence(Math.max(0, Math.min(1, entry.getConfidence() + delta)));
            }
        }
        
        public void remove(String key) {
            knowledgeBase.remove(key);
        }
        
        public void clear() {
            knowledgeBase.clear();
        }
        
        public int size() {
            return knowledgeBase.size();
        }
    }
    
    // ==================== Data Classes ====================
    
    /**
     * 记忆条目
     */
    @Data
    @Builder
    public static class MemoryEntry {
        private String content;
        private MemoryType type;
        private String metadata;
        private long timestamp;
        private double importance; // 0-1, 重要性权重
    }
    
    /**
     * 知识条目
     */
    @Data
    @Builder
    public static class KnowledgeEntry {
        private String key;
        private String knowledge;
        private KnowledgeType type;
        private double confidence; // 0-1
        private int accessCount;
        private long lastAccessTime;
        private long createdTime;
        
        public void incrementAccessCount() {
            this.accessCount++;
        }
    }
    
    /**
     * 记忆类型
     */
    public enum MemoryType {
        USER_MESSAGE,      // 用户消息
        AGENT_RESPONSE,    // Agent响应
        TOOL_RESULT,       // 工具结果
        THINKING,          // 思考过程
        PLAN,              // 执行计划
        REFLECTION,        // 反思总结
        ERROR,             // 错误信息
        SUCCESS            // 成功信息
    }
    
    /**
     * 知识类型
     */
    public enum KnowledgeType {
        FACT,              // 事实
        PATTERN,           // 模式
        RULE,              // 规则
        HEURISTIC,         // 启发式
        SUCCESS_CASE,      // 成功案例
        FAILURE_CASE,      // 失败案例
        BEST_PRACTICE      // 最佳实践
    }
}
