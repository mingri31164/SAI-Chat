/*
 * 内容安全过滤器
 */

package com.sai.chat.agent.rag.core.security;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 内容安全过滤器
 */
@Slf4j
@Component
public class ContentSafetyFilter {

    private final List<ContentRule> rules = new ArrayList<>();
    private final Map<String, Integer> violationCache = new ConcurrentHashMap<>();

    private static final double DEFAULT_MAX_RISK_SCORE = 0.7;
    private static final int MAX_INPUT_LENGTH = 10000;
    private static final int MAX_OUTPUT_LENGTH = 50000;

    public ContentSafetyFilter() {
        initDefaultRules();
    }

    /**
     * 初始化默认规则
     */
    private void initDefaultRules() {
        // 敏感词规则
        addRule(ContentRule.builder()
                .ruleId("sensitive-words")
                .name("敏感词检测")
                .category(RuleCategory.SENSITIVE)
                .severity(Severity.HIGH)
                .riskScore(0.9)
                .enabled(true)
                .build());

        // 恶意指令规则
        addRule(ContentRule.builder()
                .ruleId("prompt-injection")
                .name("Prompt注入检测")
                .category(RuleCategory.INJECTION)
                .severity(Severity.CRITICAL)
                .riskScore(1.0)
                .enabled(true)
                .build());

        // 个人信息规则
        addRule(ContentRule.builder()
                .ruleId("pii-detection")
                .name("个人信息检测")
                .category(RuleCategory.PII)
                .severity(Severity.MEDIUM)
                .riskScore(0.6)
                .enabled(true)
                .build());

        // 恶意代码规则
        addRule(ContentRule.builder()
                .ruleId("code-injection")
                .name("代码注入检测")
                .category(RuleCategory.INJECTION)
                .severity(Severity.HIGH)
                .riskScore(0.9)
                .enabled(true)
                .build());
    }

    /**
     * 过滤输入内容
     */
    public SafetyResult filterInput(String content, String userId) {
        return filter(content, userId, ContentType.INPUT);
    }

    /**
     * 过滤输出内容
     */
    public SafetyResult filterOutput(String content, String userId) {
        return filter(content, userId, ContentType.OUTPUT);
    }

    /**
     * 执行过滤
     */
    public SafetyResult filter(String content, String userId, ContentType type) {
        SafetyResult result = SafetyResult.builder()
                .content(content)
                .userId(userId)
                .contentType(type)
                .timestamp(System.currentTimeMillis())
                .violations(new ArrayList<>())
                .riskScore(0.0)
                .allowed(true)
                .build();

        // 长度检查
        if (type == ContentType.INPUT && content.length() > MAX_INPUT_LENGTH) {
            result.getViolations().add(Violation.builder()
                    .ruleId("max-length")
                    .message("输入内容过长")
                    .severity(Severity.HIGH)
                    .position(0)
                    .build());
            result.setRiskScore(1.0);
        }

        if (type == ContentType.OUTPUT && content.length() > MAX_OUTPUT_LENGTH) {
            result.getViolations().add(Violation.builder()
                    .ruleId("max-length")
                    .message("输出内容过长")
                    .severity(Severity.MEDIUM)
                    .position(0)
                    .build());
            result.setRiskScore(1.0);
        }

        // 敏感词检测
        if (containsSensitiveWords(content)) {
            result.getViolations().add(Violation.builder()
                    .ruleId("sensitive-words")
                    .message("检测到敏感词")
                    .severity(Severity.HIGH)
                    .position(-1)
                    .build());
            result.setRiskScore(0.9);
        }

        // Prompt注入检测
        if (detectPromptInjection(content)) {
            result.getViolations().add(Violation.builder()
                    .ruleId("prompt-injection")
                    .message("检测到可能的Prompt注入")
                    .severity(Severity.CRITICAL)
                    .position(-1)
                    .build());
            result.setRiskScore(1.0);
        }

        // 个人信息检测
        List<String> piiFound = detectPII(content);
        if (!piiFound.isEmpty()) {
            result.getViolations().add(Violation.builder()
                    .ruleId("pii-detection")
                    .message("检测到个人信息: " + String.join(", ", piiFound))
                    .severity(Severity.MEDIUM)
                    .position(-1)
                    .build());
            result.setRiskScore(Math.max(result.getRiskScore(), 0.6));
        }

        // 代码注入检测
        if (detectCodeInjection(content)) {
            result.getViolations().add(Violation.builder()
                    .ruleId("code-injection")
                    .message("检测到可能的代码注入")
                    .severity(Severity.HIGH)
                    .position(-1)
                    .build());
            result.setRiskScore(1.0);
        }

        // 确定是否允许
        result.setAllowed(result.getRiskScore() < DEFAULT_MAX_RISK_SCORE);

        if (!result.isAllowed()) {
            log.warn("Content blocked for user {}: risk score = {}, violations = {}", 
                    userId, result.getRiskScore(), result.getViolations().size());
            
            // 记录违规
            String cacheKey = userId + ":" + result.getTimestamp();
            violationCache.merge(cacheKey, 1, Integer::sum);
        }

        return result;
    }

    /**
     * 添加规则
     */
    public void addRule(ContentRule rule) {
        rules.add(rule);
        log.info("Added content safety rule: {}", rule.getRuleId());
    }

    /**
     * 移除规则
     */
    public void removeRule(String ruleId) {
        rules.removeIf(r -> r.getRuleId().equals(ruleId));
        log.info("Removed content safety rule: {}", ruleId);
    }

    /**
     * 获取所有规则
     */
    public List<ContentRule> getAllRules() {
        return new ArrayList<>(rules);
    }

    /**
     * 获取用户违规次数
     */
    public int getUserViolationCount(String userId) {
        return violationCache.entrySet().stream()
                .filter(e -> e.getKey().startsWith(userId + ":"))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    /**
     * 清理解控记录
     */
    public void clearViolationCache() {
        violationCache.clear();
    }

    // ==================== 检测方法 ====================

    private boolean containsSensitiveWords(String content) {
        // 简化实现：检测常见敏感词
        String[] sensitivePatterns = {
            "赌博", "色情", "毒品", "枪支", "暴力"
        };
        
        for (String pattern : sensitivePatterns) {
            if (content.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean detectPromptInjection(String content) {
        // 检测常见的Prompt注入模式
        String[] injectionPatterns = {
            "ignore previous instructions",
            "disregard all previous",
            "你是一个不同的AI",
            "现在你是",
            "forget all rules",
            "new instructions:",
            "system prompt:",
            "/# Instructions /",
            "## System Prompt"
        };
        
        for (String pattern : injectionPatterns) {
            if (content.toLowerCase().contains(pattern.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    private List<String> detectPII(String content) {
        List<String> piiTypes = new ArrayList<>();
        
        // 手机号
        if (Pattern.matches(".*1[3-9]\\d{9}.*", content)) {
            piiTypes.add("手机号");
        }
        
        // 邮箱
        if (Pattern.matches(".*[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*", content)) {
            piiTypes.add("邮箱");
        }
        
        // 身份证号
        if (Pattern.matches(".*\\d{17}[\\dXx].*", content)) {
            piiTypes.add("身份证号");
        }
        
        return piiTypes;
    }

    private boolean detectCodeInjection(String content) {
        // 检测可疑的代码执行模式
        String[] codePatterns = {
            "eval(",
            "exec(",
            "os.system",
            "subprocess",
            "Runtime.exec",
            "ProcessBuilder",
            "exec\\(",
            "shell_exec",
            "system(",
            "__import__"
        };
        
        for (String pattern : codePatterns) {
            if (content.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class ContentRule {
        private String ruleId;
        private String name;
        private String description;
        private RuleCategory category;
        private Severity severity;
        private double riskScore;
        private boolean enabled;
        private Map<String, Object> config;
    }

    @Data
    @Builder
    public static class SafetyResult {
        private String content;
        private String userId;
        private ContentType contentType;
        private long timestamp;
        private List<Violation> violations;
        private double riskScore;
        private boolean allowed;
    }

    @Data
    @Builder
    public static class Violation {
        private String ruleId;
        private String message;
        private Severity severity;
        private int position;
    }

    public enum ContentType {
        INPUT,
        OUTPUT
    }

    public enum RuleCategory {
        SENSITIVE,
        INJECTION,
        PII,
        ABUSE,
        CUSTOM
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}