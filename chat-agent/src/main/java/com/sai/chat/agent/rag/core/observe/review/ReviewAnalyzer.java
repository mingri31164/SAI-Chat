/*
 * 复盘分析器
 * 
 * 分析Agent运行结果，提取成功模式和失败教训
 */

package com.sai.chat.agent.rag.core.observe.review;

import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import com.sai.chat.agent.rag.core.observe.eval.EvalResult;
import com.sai.chat.agent.rag.core.observe.trace.AgentTrace;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 复盘分析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAnalyzer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 执行完整复盘
     */
    public ReviewReport review(AgentTrace trace, AgentResponse response, EvalResult evalResult) {
        ReviewReport report = ReviewReport.builder()
                .reviewId("review-" + UUID.randomUUID().toString().substring(0, 8))
                .traceId(trace.getTraceId())
                .sessionId(trace.getSessionId())
                .timestamp(System.currentTimeMillis())
                .success(response.isSuccess())
                .status(response.getStatus())
                .durationMs(trace.getTotalDurationMs())
                .iterations(trace.getSpanCount())
                .tokenUsage(trace.getSpanList().stream()
                        .filter(s -> s.getAttributes() != null && s.getAttributes().containsKey("tokens"))
                        .mapToLong(s -> {
                            Object tokens = s.getAttributes().get("tokens");
                            if (tokens instanceof Number) {
                                return ((Number) tokens).longValue();
                            }
                            if (tokens instanceof String) {
                                try {
                                    return Long.parseLong((String) tokens);
                                } catch (NumberFormatException e) {
                                    return 0L;
                                }
                            }
                            return 0L;
                        })
                        .sum())
                .build();

        // 分析成功因素
        if (response.isSuccess()) {
            report.setSuccessFactors(analyzeSuccessFactors(trace, response, evalResult));
            report.setLessonType(LessonType.SUCCESS_PATTERN);
        } else {
            report.setFailureReasons(analyzeFailureReasons(trace, response, evalResult));
            report.setLessonType(LessonType.FAILURE_LESSON);
        }

        // 提取关键决策点
        report.setKeyDecisions(extractKeyDecisions(trace));

        // 生成改进建议
        report.setImprovements(generateImprovements(report));

        // 计算置信度
        report.setConfidence(calculateConfidence(report));

        log.info("Generated review report: {} for trace: {}", report.getReviewId(), trace.getTraceId());

        return report;
    }

    /**
     * 批量复盘
     */
    public List<ReviewReport> batchReview(List<AgentTrace> traces) {
        return traces.stream()
                .map(trace -> {
                    // 简化实现：创建基础复盘报告
                    return ReviewReport.builder()
                            .reviewId("batch-" + trace.getTraceId())
                            .traceId(trace.getTraceId())
                            .sessionId(trace.getSessionId())
                            .timestamp(System.currentTimeMillis())
                            .success(trace.getStatus() == AgentTrace.TraceStatus.SUCCESS)
                            .status(trace.getStatus() == AgentTrace.TraceStatus.SUCCESS 
                                    ? AgentStatus.COMPLETED : AgentStatus.FAILED)
                            .durationMs(trace.getTotalDurationMs())
                            .iterations(trace.getSpanCount())
                            .keyDecisions(List.of())
                            .successFactors(List.of())
                            .failureReasons(List.of())
                            .improvements(List.of())
                            .confidence(0.5)
                            .lessonType(trace.getStatus() == AgentTrace.TraceStatus.SUCCESS 
                                    ? LessonType.SUCCESS_PATTERN : LessonType.FAILURE_LESSON)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 聚合多次复盘结果
     */
    public AggregatedReview aggregateReviews(List<ReviewReport> reports) {
        if (reports.isEmpty()) {
            return AggregatedReview.builder().build();
        }

        long successCount = reports.stream().filter(ReviewReport::isSuccess).count();
        double avgDuration = reports.stream()
                .mapToLong(ReviewReport::getDurationMs)
                .average()
                .orElse(0);

        List<ReviewReport> successReports = reports.stream()
                .filter(ReviewReport::isSuccess)
                .collect(Collectors.toList());

        List<ReviewReport> failReports = reports.stream()
                .filter(r -> !r.isSuccess())
                .collect(Collectors.toList());

        // 提取共同成功因素
        Set<String> commonSuccessFactors = extractCommonFactors(successReports, true);

        // 提取共同失败原因
        Set<String> commonFailureReasons = extractCommonFactors(failReports, false);

        return AggregatedReview.builder()
                .generatedTime(System.currentTimeMillis())
                .totalReviews(reports.size())
                .successRate((double) successCount / reports.size())
                .averageDurationMs((long) avgDuration)
                .commonSuccessFactors(commonSuccessFactors)
                .commonFailureReasons(commonFailureReasons)
                .successPatternCount(commonSuccessFactors.size())
                .failureLessonCount(commonFailureReasons.size())
                .recommendations(generateAggregatedRecommendations(commonSuccessFactors, commonFailureReasons))
                .build();
    }

    private List<String> analyzeSuccessFactors(AgentTrace trace, AgentResponse response, EvalResult evalResult) {
        List<String> factors = new ArrayList<>();

        // 迭代次数合理
        int iterations = trace.getSpanCount();
        if (iterations >= 1 && iterations <= 5) {
            factors.add("迭代次数合理: " + iterations + "次");
        }

        // 响应质量高
        if (evalResult != null && evalResult.getScore() >= 80) {
            factors.add("评测得分高: " + evalResult.getScore());
        }

        // Token消耗合理
        if (response.getTotalTokens() > 0 && response.getTotalTokens() < 5000) {
            factors.add("Token消耗合理: " + response.getTotalTokens());
        }

        // 工具使用正确
        if (response.getToolCallTrace() != null && !response.getToolCallTrace().isEmpty()) {
            factors.add("工具调用有效: " + response.getToolCallTrace().size() + "次");
        }

        // 快速完成
        if (trace.getTotalDurationMs() < 5000) {
            factors.add("响应速度快: " + trace.getTotalDurationMs() + "ms");
        }

        return factors;
    }

    private List<String> analyzeFailureReasons(AgentTrace trace, AgentResponse response, EvalResult evalResult) {
        List<String> reasons = new ArrayList<>();

        // 迭代次数过多
        int iterations = trace.getSpanCount();
        if (iterations > 10) {
            reasons.add("迭代次数过多: " + iterations + "次");
        }

        // 响应时间过长
        if (trace.getTotalDurationMs() > 30000) {
            reasons.add("响应超时: " + trace.getTotalDurationMs() + "ms");
        }

        // 评测得分低
        if (evalResult != null && evalResult.getScore() < 60) {
            reasons.add("答案质量低: 得分 " + evalResult.getScore());
        }

        // 工具调用失败
        if (response.getToolCallTrace() != null) {
            long failedTools = response.getToolCallTrace().stream()
                    .filter(r -> r.getResult() != null && !r.getResult().isSuccess())
                    .count();
            if (failedTools > 0) {
                reasons.add("工具调用失败: " + failedTools + "次");
            }
        }

        // 错误消息
        if (response.getErrorMessage() != null) {
            reasons.add("执行错误: " + truncate(response.getErrorMessage(), 100));
        }

        return reasons;
    }

    private List<String> extractKeyDecisions(AgentTrace trace) {
        List<String> decisions = new ArrayList<>();

        for (AgentTrace.Span span : trace.getSpanList()) {
            if (span.getType() == AgentTrace.Span.SpanType.LLM_REASONING) {
                String reasoning = span.getName();
                if (reasoning != null && !reasoning.isEmpty()) {
                    decisions.add("推理: " + truncate(reasoning, 100));
                }
            } else if (span.getType() == AgentTrace.Span.SpanType.TOOL_CALL) {
                decisions.add("工具: " + span.getName());
            }
        }

        return decisions;
    }

    private List<String> generateImprovements(ReviewReport report) {
        List<String> improvements = new ArrayList<>();

        if (!report.isSuccess()) {
            // 失败情况下的改进建议
            if (report.getFailureReasons() != null) {
                for (String reason : report.getFailureReasons()) {
                    if (reason.contains("迭代次数过多")) {
                        improvements.add("考虑添加任务分解策略");
                        improvements.add("优化Prompt减少无效迭代");
                    }
                    if (reason.contains("超时")) {
                        improvements.add("增加超时处理和重试逻辑");
                        improvements.add("考虑使用更快的模型");
                    }
                    if (reason.contains("质量低")) {
                        improvements.add("优化系统提示词");
                        improvements.add("增加参考信息");
                    }
                }
            }
        } else {
            // 成功情况下的进一步优化
            if (report.getDurationMs() > 10000) {
                improvements.add("响应时间有优化空间");
            }
            if (report.getTokenUsage() > 5000) {
                improvements.add("考虑压缩上下文减少Token消耗");
            }
        }

        return improvements;
    }

    private double calculateConfidence(ReviewReport report) {
        double confidence = 0.5;

        // 基于信息完整性调整置信度
        if (report.getSuccessFactors() != null && !report.getSuccessFactors().isEmpty()) {
            confidence += 0.1;
        }
        if (report.getFailureReasons() != null && !report.getFailureReasons().isEmpty()) {
            confidence += 0.1;
        }
        if (report.getKeyDecisions() != null && report.getKeyDecisions().size() >= 3) {
            confidence += 0.1;
        }

        return Math.min(1.0, confidence);
    }

    private Set<String> extractCommonFactors(List<ReviewReport> reports, boolean successFactors) {
        Map<String, Integer> factorCounts = new HashMap<>();

        for (ReviewReport report : reports) {
            List<String> factors = successFactors ? report.getSuccessFactors() : report.getFailureReasons();
            if (factors != null) {
                for (String factor : factors) {
                    // 简化：只取关键部分
                    String key = extractKeyPhrase(factor);
                    factorCounts.merge(key, 1, Integer::sum);
                }
            }
        }

        // 返回出现次数超过50%的因素
        int threshold = reports.size() / 2;
        return factorCounts.entrySet().stream()
                .filter(e -> e.getValue() >= threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private List<String> generateAggregatedRecommendations(Set<String> successFactors, 
                                                          Set<String> failureReasons) {
        List<String> recommendations = new ArrayList<>();

        // 基于成功因素建议
        for (String factor : successFactors) {
            if (factor.contains("迭代次数")) {
                recommendations.add("保持当前的迭代策略");
            }
            if (factor.contains("Token")) {
                recommendations.add("继续使用当前的上下文压缩策略");
            }
        }

        // 基于失败原因建议
        for (String reason : failureReasons) {
            if (reason.contains("超时")) {
                recommendations.add("添加超时处理和快速失败机制");
            }
            if (reason.contains("迭代")) {
                recommendations.add("添加迭代限制和早期退出策略");
            }
            if (reason.contains("质量")) {
                recommendations.add("优化Prompt和增加评估反馈");
            }
        }

        return recommendations.stream().distinct().collect(Collectors.toList());
    }

    private String extractKeyPhrase(String factor) {
        // 简化实现：取第一个冒号后的内容或前20个字符
        int colonIndex = factor.indexOf(':');
        if (colonIndex > 0 && colonIndex < 30) {
            return factor.substring(0, colonIndex);
        }
        return factor.substring(0, Math.min(20, factor.length()));
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class ReviewReport {
        private String reviewId;
        private String traceId;
        private String sessionId;
        private long timestamp;
        private boolean success;
        private AgentStatus status;
        private long durationMs;
        private int iterations;
        private long tokenUsage;
        private LessonType lessonType;
        private List<String> keyDecisions;
        private List<String> successFactors;
        private List<String> failureReasons;
        private List<String> improvements;
        private double confidence;
    }

    @Data
    @Builder
    public static class AggregatedReview {
        private long generatedTime;
        private int totalReviews;
        private double successRate;
        private long averageDurationMs;
        private Set<String> commonSuccessFactors;
        private Set<String> commonFailureReasons;
        private int successPatternCount;
        private int failureLessonCount;
        private List<String> recommendations;
    }

    public enum LessonType {
        SUCCESS_PATTERN,
        FAILURE_LESSON,
        NEUTRAL
    }
}