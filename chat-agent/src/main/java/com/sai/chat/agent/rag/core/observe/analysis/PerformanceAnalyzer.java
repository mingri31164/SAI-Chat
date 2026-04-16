/*
 * 性能分析器
 *
 * 分析Agent运行性能，识别瓶颈，提供优化建议
 */

package com.sai.chat.agent.rag.core.observe.analysis;

import com.sai.chat.agent.rag.core.observe.metrics.MetricsCollector;
import com.sai.chat.agent.rag.core.observe.trace.AgentTrace;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 性能分析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceAnalyzer {

    private final MetricsCollector metricsCollector;

    // 性能阈值配置
    private static final long SLOW_LATENCY_THRESHOLD_MS = 3000;
    private static final double HIGH_ERROR_RATE_THRESHOLD = 0.1;
    private static final long HIGH_P99_THRESHOLD_MS = 5000;
    private static final int EXCESSIVE_ITERATIONS = 10;

    /**
     * 执行完整性能分析
     */
    public PerformanceReport analyze() {
        return analyze(System.currentTimeMillis() - 3600000); // 最近1小时
    }

    /**
     * 执行指定时间范围的性能分析
     */
    public PerformanceReport analyze(long sinceTime) {
        MetricsCollector.MetricsSnapshot snapshot = metricsCollector.getGlobalSnapshot();

        List<PerformanceIssue> issues = new ArrayList<>();
        List<OptimizationSuggestion> suggestions = new ArrayList<>();

        // 分析各项指标
        analyzeLatency(snapshot, issues, suggestions);
        analyzeThroughput(snapshot, issues, suggestions);
        analyzeErrorPatterns(snapshot, issues, suggestions);
        analyzeResourceUsage(snapshot, issues, suggestions);

        // 计算综合评分
        double overallScore = calculateOverallScore(snapshot, issues);

        return PerformanceReport.builder()
                .timestamp(System.currentTimeMillis())
                .timeRangeSince(sinceTime)
                .snapshot(snapshot)
                .issues(issues)
                .suggestions(suggestions)
                .overallScore(overallScore)
                .healthStatus(determineHealthStatus(overallScore, issues))
                .build();
    }

    /**
     * 分析单个追踪的性能问题
     */
    public TracePerformanceAnalysis analyzeTrace(AgentTrace trace) {
        List<PerformanceIssue> issues = new ArrayList<>();
        List<String> bottlenecks = new ArrayList<>();

        long totalDuration = trace.getTotalDurationMs();

        // 分析各阶段耗时
        Map<String, Long> phaseDurations = analyzePhaseDurations(trace);
        for (Map.Entry<String, Long> entry : phaseDurations.entrySet()) {
            String phase = entry.getKey();
            long duration = entry.getValue();
            double percentage = totalDuration > 0 ? (double) duration / totalDuration * 100 : 0;

            if (percentage > 50 && duration > 1000) {
                bottlenecks.add(String.format("%s 占比 %.1f%% (%dms)", phase, percentage, duration));

                PerformanceIssue issue = PerformanceIssue.builder()
                        .type(IssueType.BOTTLENECK)
                        .severity(determineSeverity(duration, percentage))
                        .description(String.format("%s 阶段耗时过长: %dms (占比 %.1f%%)",
                                phase, duration, percentage))
                        .metric(phase + "_duration_ms")
                        .value(duration)
                        .threshold(1000L)
                        .build();
                issues.add(issue);
            }
        }

        // 检查迭代次数
        int iterations = trace.getSpanCount();
        if (iterations > EXCESSIVE_ITERATIONS) {
            bottlenecks.add("迭代次数过多: " + iterations);
            issues.add(PerformanceIssue.builder()
                    .type(IssueType.INEFFICIENCY)
                    .severity(Severity.LOW)
                    .description("迭代次数过多: " + iterations)
                    .metric("iteration_count")
                    .value(iterations)
                    .threshold(EXCESSIVE_ITERATIONS)
                    .build());
        }

        // 检查Token消耗
        long totalTokens = calculateTotalTokens(trace);
        if (totalTokens > 8000) {
            bottlenecks.add("Token消耗较高: " + totalTokens);
            issues.add(PerformanceIssue.builder()
                    .type(IssueType.RESOURCE_USAGE)
                    .severity(Severity.INFO)
                    .description("Token消耗较高: " + totalTokens)
                    .metric("total_tokens")
                    .value(totalTokens)
                    .threshold(8000L)
                    .build());
        }

        return TracePerformanceAnalysis.builder()
                .traceId(trace.getTraceId())
                .sessionId(trace.getSessionId())
                .totalDurationMs(totalDuration)
                .phaseDurations(phaseDurations)
                .bottlenecks(bottlenecks)
                .issues(issues)
                .iterations(iterations)
                .totalTokens(totalTokens)
                .build();
    }

    private void analyzeLatency(MetricsCollector.MetricsSnapshot snapshot,
                               List<PerformanceIssue> issues,
                               List<OptimizationSuggestion> suggestions) {
        double avgLatency = snapshot.getAverageDurationMs();
        double p50 = snapshot.getP50LatencyMs();
        double p95 = snapshot.getP95LatencyMs();
        double p99 = snapshot.getP99LatencyMs();

        // 平均延迟过高
        if (avgLatency > SLOW_LATENCY_THRESHOLD_MS) {
            issues.add(PerformanceIssue.builder()
                    .type(IssueType.LATENCY)
                    .severity(Severity.HIGH)
                    .description("平均响应延迟过高: " + String.format("%.0fms", avgLatency))
                    .metric("avg_latency_ms")
                    .value((long) avgLatency)
                    .threshold(SLOW_LATENCY_THRESHOLD_MS)
                    .build());

            suggestions.add(OptimizationSuggestion.builder()
                    .category("LATENCY")
                    .priority(Priority.HIGH)
                    .title("优化响应延迟")
                    .description("当前平均延迟 " + String.format("%.0fms", avgLatency) +
                            " 超过目标阈值 " + SLOW_LATENCY_THRESHOLD_MS + "ms")
                    .suggestedActions(List.of(
                            "考虑使用更快的LLM模型",
                            "优化提示词减少生成token数量",
                            "增加缓存减少重复计算"
                    ))
                    .expectedImprovement("延迟降低 30-50%")
                    .build());
        }

        // P99延迟过高
        if (p99 > HIGH_P99_THRESHOLD_MS) {
            issues.add(PerformanceIssue.builder()
                    .type(IssueType.LATENCY)
                    .severity(Severity.MEDIUM)
                    .description("P99延迟过高: " + String.format("%.0fms", p99))
                    .metric("p99_latency_ms")
                    .value((long) p99)
                    .threshold(HIGH_P99_THRESHOLD_MS)
                    .build());

            suggestions.add(OptimizationSuggestion.builder()
                    .category("LATENCY")
                    .priority(Priority.MEDIUM)
                    .title("改善长尾延迟")
                    .description("99%的请求延迟超过 " + HIGH_P99_THRESHOLD_MS + "ms")
                    .suggestedActions(List.of(
                            "添加请求超时限制",
                            "优化最坏情况的处理逻辑",
                            "使用请求优先级队列"
                    ))
                    .expectedImprovement("P99降低 40-60%")
                    .build());
        }

        // P95与P50差距大
        if (p95 > p50 * 2) {
            suggestions.add(OptimizationSuggestion.builder()
                    .category("LATENCY")
                    .priority(Priority.LOW)
                    .title("减少延迟波动")
                    .description("P95/P50比率较大，表明存在性能波动")
                    .suggestedActions(List.of(
                            "标准化处理流程",
                            "减少外部依赖",
                            "增加资源预热"
                    ))
                    .expectedImprovement("延迟波动降低 50%")
                    .build());
        }
    }

    private void analyzeThroughput(MetricsCollector.MetricsSnapshot snapshot,
                                  List<PerformanceIssue> issues,
                                  List<OptimizationSuggestion> suggestions) {
        double qps = snapshot.getQps();

        if (qps < 1 && snapshot.getTotalRequests() > 100) {
            suggestions.add(OptimizationSuggestion.builder()
                    .category("THROUGHPUT")
                    .priority(Priority.MEDIUM)
                    .title("提升吞吐量")
                    .description("当前QPS较低: " + String.format("%.2f", qps))
                    .suggestedActions(List.of(
                            "增加并发处理能力",
                            "优化数据库查询",
                            "使用异步处理"
                    ))
                    .expectedImprovement("QPS提升 2-3倍")
                    .build());
        }
    }

    private void analyzeErrorPatterns(MetricsCollector.MetricsSnapshot snapshot,
                                     List<PerformanceIssue> issues,
                                     List<OptimizationSuggestion> suggestions) {
        double errorRate = snapshot.getErrorRate();

        if (errorRate > HIGH_ERROR_RATE_THRESHOLD) {
            issues.add(PerformanceIssue.builder()
                    .type(IssueType.ERROR_RATE)
                    .severity(Severity.CRITICAL)
                    .description("错误率过高: " + String.format("%.1f%%", errorRate * 100))
                    .metric("error_rate")
                    .value((long) (errorRate * 100))
                    .threshold(HIGH_ERROR_RATE_THRESHOLD)
                    .build());

            suggestions.add(OptimizationSuggestion.builder()
                    .category("RELIABILITY")
                    .priority(Priority.HIGH)
                    .title("降低错误率")
                    .description("当前错误率 " + String.format("%.1f%%", errorRate * 100) +
                            " 超过可接受阈值 " + String.format("%.1f%%", HIGH_ERROR_RATE_THRESHOLD * 100))
                    .suggestedActions(List.of(
                            "分析错误日志识别根本原因",
                            "添加输入验证",
                            "增强错误处理和重试逻辑"
                    ))
                    .expectedImprovement("错误率降低至 1% 以下")
                    .build());
        }

        // 超时率分析
        double timeoutRate = snapshot.getTimeoutRate();
        if (timeoutRate > 0.05) {
            suggestions.add(OptimizationSuggestion.builder()
                    .category("RELIABILITY")
                    .priority(Priority.HIGH)
                    .title("减少超时")
                    .description("超时率: " + String.format("%.1f%%", timeoutRate * 100))
                    .suggestedActions(List.of(
                            "增加超时阈值或优化处理逻辑",
                            "添加请求优先级",
                            "使用熔断器保护"
                    ))
                    .expectedImprovement("超时率降低至 1% 以下")
                    .build());
        }
    }

    private void analyzeResourceUsage(MetricsCollector.MetricsSnapshot snapshot,
                                     List<PerformanceIssue> issues,
                                     List<OptimizationSuggestion> suggestions) {
        double avgTokens = snapshot.getAverageTokensPerRequest();
        double totalCost = snapshot.getTotalCostYuan();

        if (avgTokens > 3000) {
            issues.add(PerformanceIssue.builder()
                    .type(IssueType.RESOURCE_USAGE)
                    .severity(Severity.MEDIUM)
                    .description("平均Token消耗较高: " + String.format("%.0f", avgTokens))
                    .metric("avg_tokens")
                    .value((long) avgTokens)
                    .threshold(3000L)
                    .build());

            suggestions.add(OptimizationSuggestion.builder()
                    .category("COST")
                    .priority(Priority.MEDIUM)
                    .title("优化Token消耗")
                    .description("每次请求平均消耗 " + String.format("%.0f", avgTokens) + " tokens")
                    .suggestedActions(List.of(
                            "精简系统提示词",
                            "优化上下文压缩策略",
                            "使用更高效的模型"
                    ))
                    .expectedImprovement("Token消耗降低 20-40%")
                    .build());
        }

        if (totalCost > 100) {
            suggestions.add(OptimizationSuggestion.builder()
                    .category("COST")
                    .priority(Priority.LOW)
                    .title("控制成本")
                    .description("累计成本: ¥" + String.format("%.2f", totalCost))
                    .suggestedActions(List.of(
                            "实施Token预算限制",
                            "使用缓存减少重复调用",
                            "选择性价比更高的模型"
                    ))
                    .expectedImprovement("成本降低 30-50%")
                    .build());
        }
    }

    private double calculateOverallScore(MetricsCollector.MetricsSnapshot snapshot,
                                       List<PerformanceIssue> issues) {
        double score = 100.0;

        // 扣分规则
        for (PerformanceIssue issue : issues) {
            switch (issue.getSeverity()) {
                case CRITICAL: score -= 20; break;
                case HIGH: score -= 10; break;
                case MEDIUM: score -= 5; break;
                case LOW: score -= 2; break;
                case INFO: score -= 1; break;
            }
        }

        // 基于实际指标的额外加分
        if (snapshot.getSuccessRate() > 0.99) score += 5;
        if (snapshot.getAverageDurationMs() < 1000) score += 5;
        if (snapshot.getP99LatencyMs() < 3000) score += 3;

        return Math.max(0, Math.min(100, score));
    }

    private HealthStatus determineHealthStatus(double score, List<PerformanceIssue> issues) {
        boolean hasCritical = issues.stream()
                .anyMatch(i -> i.getSeverity() == Severity.CRITICAL);

        if (hasCritical) return HealthStatus.CRITICAL;
        if (score >= 90) return HealthStatus.HEALTHY;
        if (score >= 70) return HealthStatus.WARNING;
        if (score >= 50) return HealthStatus.DEGRADED;
        return HealthStatus.CRITICAL;
    }

    private Severity determineSeverity(long value, double percentage) {
        if (percentage > 80 || value > 5000) return Severity.HIGH;
        if (percentage > 60 || value > 2000) return Severity.MEDIUM;
        return Severity.LOW;
    }

    private Map<String, Long> analyzePhaseDurations(AgentTrace trace) {
        Map<String, Long> phaseDurations = new HashMap<>();
        phaseDurations.put("LLM推理", 0L);
        phaseDurations.put("工具调用", 0L);
        phaseDurations.put("其他", 0L);

        for (AgentTrace.Span span : trace.getSpanList()) {
            if (span.getType() == AgentTrace.Span.SpanType.LLM_REASONING) {
                phaseDurations.merge("LLM推理", span.getDurationMs(), Long::sum);
            } else if (span.getType() == AgentTrace.Span.SpanType.TOOL_CALL) {
                phaseDurations.merge("工具调用", span.getDurationMs(), Long::sum);
            } else {
                phaseDurations.merge("其他", span.getDurationMs(), Long::sum);
            }
        }

        return phaseDurations;
    }

    private long calculateTotalTokens(AgentTrace trace) {
        // 从追踪中提取token消耗
        return trace.getSpanList().stream()
                .filter(s -> s.getAttributes() != null)
                .mapToLong(s -> {
                    Object tokens = s.getAttributes().get("tokens");
                    return tokens instanceof Number ? ((Number) tokens).longValue() : 0;
                })
                .sum();
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class PerformanceReport {
        private long timestamp;
        private long timeRangeSince;
        private MetricsCollector.MetricsSnapshot snapshot;
        private List<PerformanceIssue> issues;
        private List<OptimizationSuggestion> suggestions;
        private double overallScore;
        private HealthStatus healthStatus;
    }

    @Data
    @Builder
    public static class PerformanceIssue {
        private IssueType type;
        private Severity severity;
        private String description;
        private String metric;
        private double value;
        private double threshold;
    }

    @Data
    @Builder
    public static class OptimizationSuggestion {
        private String category;
        private Priority priority;
        private String title;
        private String description;
        private List<String> suggestedActions;
        private String expectedImprovement;
    }

    @Data
    @Builder
    public static class TracePerformanceAnalysis {
        private String traceId;
        private String sessionId;
        private long totalDurationMs;
        private Map<String, Long> phaseDurations;
        private List<String> bottlenecks;
        private List<PerformanceIssue> issues;
        private int iterations;
        private long totalTokens;
    }

    public enum IssueType {
        LATENCY,
        THROUGHPUT,
        ERROR_RATE,
        RESOURCE_USAGE,
        BOTTLENECK,
        INEFFICIENCY
    }

    public enum Severity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }

    public enum Priority {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum HealthStatus {
        HEALTHY,
        WARNING,
        DEGRADED,
        CRITICAL
    }
}
