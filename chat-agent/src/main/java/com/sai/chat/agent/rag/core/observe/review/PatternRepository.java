/*
 * 模式沉淀管理器
 * 
 * 管理成功模式和失败教训的持久化存储
 */

package com.sai.chat.agent.rag.core.observe.review;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模式沉淀管理器
 */
@Slf4j
@Component
public class PatternRepository {

    // 成功模式库
    private final Map<String, SuccessPattern> successPatterns = new ConcurrentHashMap<>();
    
    // 失败教训库
    private final Map<String, FailureLesson> failureLessons = new ConcurrentHashMap<>();
    
    // 模式标签索引
    private final Map<String, Set<String>> tagIndex = new ConcurrentHashMap<>();

    // ==================== 模式管理 ====================

    /**
     * 添加成功模式
     */
    public SuccessPattern addPattern(SuccessPattern pattern) {
        pattern.setCreatedTime(System.currentTimeMillis());
        pattern.setAccessCount(0);
        pattern.setLastAccessTime(System.currentTimeMillis());
        
        successPatterns.put(pattern.getPatternId(), pattern);
        indexPattern(pattern);
        
        log.info("Added success pattern: {} - {}", pattern.getPatternId(), pattern.getTitle());
        
        return pattern;
    }

    /**
     * 添加失败教训
     */
    public FailureLesson addLesson(FailureLesson lesson) {
        lesson.setCreatedTime(System.currentTimeMillis());
        lesson.setOccurrenceCount(1);
        lesson.setLastOccurrenceTime(System.currentTimeMillis());
        
        failureLessons.put(lesson.getLessonId(), lesson);
        indexLesson(lesson);
        
        log.info("Added failure lesson: {} - {}", lesson.getLessonId(), lesson.getTitle());
        
        return lesson;
    }

    /**
     * 获取成功模式
     */
    public SuccessPattern getPattern(String patternId) {
        SuccessPattern pattern = successPatterns.get(patternId);
        if (pattern != null) {
            pattern.incrementAccessCount();
            pattern.setLastAccessTime(System.currentTimeMillis());
        }
        return pattern;
    }

    /**
     * 获取失败教训
     */
    public FailureLesson getLesson(String lessonId) {
        FailureLesson lesson = failureLessons.get(lessonId);
        if (lesson != null) {
            lesson.setLastOccurrenceTime(System.currentTimeMillis());
            lesson.incrementOccurrenceCount();
        }
        return lesson;
    }

    /**
     * 搜索成功模式
     */
    public List<SuccessPattern> searchPatterns(String query) {
        String lowerQuery = query.toLowerCase();
        
        return successPatterns.values().stream()
                .filter(p -> matchesPattern(p, lowerQuery))
                .sorted((a, b) -> compareByRelevance(a, b, lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * 搜索失败教训
     */
    public List<FailureLesson> searchLessons(String query) {
        String lowerQuery = query.toLowerCase();
        
        return failureLessons.values().stream()
                .filter(l -> matchesLesson(l, lowerQuery))
                .sorted((a, b) -> Integer.compare(b.getOccurrenceCount(), a.getOccurrenceCount()))
                .collect(Collectors.toList());
    }

    /**
     * 获取相关模式
     */
    public List<SuccessPattern> getRelatedPatterns(String context) {
        List<SuccessPattern> patterns = new ArrayList<>();
        
        // 按标签匹配
        for (Map.Entry<String, Set<String>> entry : tagIndex.entrySet()) {
            if (context.toLowerCase().contains(entry.getKey().toLowerCase())) {
                for (String patternId : entry.getValue()) {
                    SuccessPattern pattern = successPatterns.get(patternId);
                    if (pattern != null && !patterns.contains(pattern)) {
                        patterns.add(pattern);
                    }
                }
            }
        }
        
        return patterns;
    }

    /**
     * 获取所有成功模式
     */
    public List<SuccessPattern> getAllPatterns() {
        return new ArrayList<>(successPatterns.values());
    }

    /**
     * 获取所有失败教训
     */
    public List<FailureLesson> getAllLessons() {
        return new ArrayList<>(failureLessons.values());
    }

    /**
     * 获取高价值模式
     */
    public List<SuccessPattern> getTopPatterns(int limit) {
        return successPatterns.values().stream()
                .sorted((a, b) -> {
                    int scoreCompare = Double.compare(b.getEffectivenessScore(), a.getEffectivenessScore());
                    if (scoreCompare != 0) return scoreCompare;
                    return Integer.compare(b.getUsageCount(), a.getUsageCount());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 更新模式效果评分
     */
    public void updatePatternScore(String patternId, double delta) {
        SuccessPattern pattern = successPatterns.get(patternId);
        if (pattern != null) {
            pattern.setEffectivenessScore(Math.max(0, Math.min(100, 
                    pattern.getEffectivenessScore() + delta)));
            log.debug("Updated pattern {} score to {}", patternId, pattern.getEffectivenessScore());
        }
    }

    /**
     * 删除模式
     */
    public void removePattern(String patternId) {
        SuccessPattern removed = successPatterns.remove(patternId);
        if (removed != null) {
            removeFromIndex(removed);
            log.info("Removed pattern: {}", patternId);
        }
    }

    /**
     * 删除教训
     */
    public void removeLesson(String lessonId) {
        FailureLesson removed = failureLessons.remove(lessonId);
        if (removed != null) {
            removeLessonFromIndex(removed);
            log.info("Removed lesson: {}", lessonId);
        }
    }

    /**
     * 导出模式到知识库
     */
    public Map<String, Object> exportPatterns() {
        return Map.of(
                "successPatterns", successPatterns.values().stream()
                        .map(p -> Map.of(
                                "id", p.getPatternId(),
                                "title", p.getTitle(),
                                "pattern", p.getPattern(),
                                "context", p.getContext(),
                                "effectiveness", p.getEffectivenessScore()
                        ))
                        .collect(Collectors.toList()),
                "failureLessons", failureLessons.values().stream()
                        .map(l -> Map.of(
                                "id", l.getLessonId(),
                                "title", l.getTitle(),
                                "lesson", l.getLesson(),
                                "prevention", l.getPrevention()
                        ))
                        .collect(Collectors.toList()),
                "exportTime", System.currentTimeMillis()
        );
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        double avgEffectiveness = successPatterns.values().stream()
                .mapToDouble(SuccessPattern::getEffectivenessScore)
                .average()
                .orElse(0);

        int maxOccurrence = failureLessons.values().stream()
                .mapToInt(FailureLesson::getOccurrenceCount)
                .max()
                .orElse(0);

        return Map.of(
                "totalPatterns", successPatterns.size(),
                "totalLessons", failureLessons.size(),
                "averageEffectiveness", avgEffectiveness,
                "mostCommonLessonOccurrence", maxOccurrence,
                "indexedTags", tagIndex.size()
        );
    }

    // ==================== 私有方法 ====================

    private void indexPattern(SuccessPattern pattern) {
        for (String tag : pattern.getTags()) {
            tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(pattern.getPatternId());
        }
    }

    private void indexLesson(FailureLesson lesson) {
        for (String tag : lesson.getTags()) {
            tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(lesson.getLessonId());
        }
    }

    private void removeFromIndex(SuccessPattern pattern) {
        for (String tag : pattern.getTags()) {
            Set<String> ids = tagIndex.get(tag);
            if (ids != null) {
                ids.remove(pattern.getPatternId());
            }
        }
    }

    private void removeLessonFromIndex(FailureLesson lesson) {
        for (String tag : lesson.getTags()) {
            Set<String> ids = tagIndex.get(tag);
            if (ids != null) {
                ids.remove(lesson.getLessonId());
            }
        }
    }

    private boolean matchesPattern(SuccessPattern pattern, String query) {
        return pattern.getTitle().toLowerCase().contains(query)
                || pattern.getPattern().toLowerCase().contains(query)
                || pattern.getContext().toLowerCase().contains(query)
                || pattern.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
    }

    private boolean matchesLesson(FailureLesson lesson, String query) {
        return lesson.getTitle().toLowerCase().contains(query)
                || lesson.getLesson().toLowerCase().contains(query)
                || lesson.getPrevention().toLowerCase().contains(query)
                || lesson.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
    }

    private int compareByRelevance(SuccessPattern a, SuccessPattern b, String query) {
        // 优先匹配标题
        boolean aTitleMatch = a.getTitle().toLowerCase().contains(query);
        boolean bTitleMatch = b.getTitle().toLowerCase().contains(query);
        if (aTitleMatch != bTitleMatch) return aTitleMatch ? -1 : 1;

        // 然后按效果评分
        return Double.compare(b.getEffectivenessScore(), a.getEffectivenessScore());
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class SuccessPattern {
        private String patternId;
        private String title;
        private String pattern;
        private String context;
        private String triggerCondition;
        private List<String> steps;
        private double effectivenessScore;
        private int usageCount;
        private List<String> tags;
        private long createdTime;
        private long lastAccessTime;
        private int accessCount;
        
        public void incrementAccessCount() {
            this.accessCount++;
            this.usageCount++;
        }
    }

    @Data
    @Builder
    public static class FailureLesson {
        private String lessonId;
        private String title;
        private String lesson;
        private String rootCause;
        private String prevention;
        private List<String> symptoms;
        private int occurrenceCount;
        private List<String> tags;
        private long createdTime;
        private long lastOccurrenceTime;
        
        public void incrementOccurrenceCount() {
            this.occurrenceCount++;
        }
    }
}