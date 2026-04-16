/*
 * 指标导出器
 */

package com.sai.chat.agent.rag.core.observe.metrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 指标导出器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConsoleExporter implements MetricsCollector.MetricsReporter {

    private final MetricsCollector metricsCollector;

    @Scheduled(fixedRate = 60000)
    public void exportToConsole() {
        MetricsCollector.MetricsSnapshot snapshot = metricsCollector.getGlobalSnapshot();
        printMetricsSummary(snapshot);
    }

    @Override
    public void report(MetricsCollector.MetricsSnapshot snapshot) {
        if (log.isInfoEnabled()) {
            printMetricsSummary(snapshot);
        }
    }

    private void printMetricsSummary(MetricsCollector.MetricsSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════\n");
        sb.append("                    Agent 指标报告                         \n");
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append(String.format("  时间: %s\n", java.time.LocalDateTime.now()));
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("【请求统计】\n");
        sb.append(String.format("  总请求数:     %d\n", snapshot.getTotalRequests()));
        sb.append(String.format("  成功:        %d (%.2f%%)\n", snapshot.getSuccessRequests(), snapshot.getSuccessRate() * 100));
        sb.append(String.format("  失败:        %d (%.2f%%)\n", snapshot.getFailedRequests(), snapshot.getErrorRate() * 100));
        sb.append(String.format("  超时:        %d (%.2f%%)\n", snapshot.getTimeoutRequests(), snapshot.getTimeoutRate() * 100));
        sb.append(String.format("  QPS:         %.2f\n", snapshot.getQps()));
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("【性能统计】\n");
        sb.append(String.format("  平均延迟:    %.2f ms\n", snapshot.getAverageDurationMs()));
        sb.append(String.format("  P50延迟:    %.2f ms\n", snapshot.getP50LatencyMs()));
        sb.append(String.format("  P95延迟:    %.2f ms\n", snapshot.getP95LatencyMs()));
        sb.append(String.format("  P99延迟:    %.2f ms\n", snapshot.getP99LatencyMs()));
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("【LLM 统计】\n");
        sb.append(String.format("  调用次数:    %d\n", snapshot.getLlmCalls()));
        sb.append(String.format("  平均耗时:    %.2f ms\n", snapshot.getAverageLlmDurationMs()));
        sb.append(String.format("  总Token:     %d\n", snapshot.getTotalTokens()));
        sb.append(String.format("  平均Token:   %.2f\n", snapshot.getAverageTokensPerRequest()));
        sb.append(String.format("  总成本:      ¥%.4f\n", snapshot.getTotalCostYuan()));
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("【工具统计】\n");
        sb.append(String.format("  调用次数:    %d\n", snapshot.getToolCalls()));
        sb.append(String.format("  平均耗时:    %.2f ms\n", snapshot.getAverageToolDurationMs()));
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("【会话统计】\n");
        sb.append(String.format("  活跃会话:    %d\n", snapshot.getActiveSessions()));
        sb.append("═══════════════════════════════════════════════════════════\n");
        log.info(sb.toString());
    }

    public void printToolMetrics() {
        Map<String, MetricsCollector.ToolMetrics> toolMetricsMap = metricsCollector.getAllToolMetrics();
        if (toolMetricsMap.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════\n");
        sb.append("                    工具指标详情                            \n");
        sb.append("═══════════════════════════════════════════════════════════\n");

        for (Map.Entry<String, MetricsCollector.ToolMetrics> entry : toolMetricsMap.entrySet()) {
            MetricsCollector.ToolMetrics tool = entry.getValue();
            sb.append(String.format("  %s (%s):\n", tool.getToolName(), tool.getToolId()));
            sb.append(String.format("    调用次数:    %d\n", tool.getTotalCalls().get()));
            sb.append(String.format("    成功率:      %.2f%%\n", tool.getSuccessRate() * 100));
            sb.append(String.format("    平均耗时:    %.2f ms\n", tool.getAverageDurationMs()));
            sb.append(String.format("    耗时范围:    %d - %d ms\n", tool.getMinDurationMs().get(), tool.getMaxDurationMs().get()));
            sb.append("───────────────────────────────────────────────────────────\n");
        }

        log.info(sb.toString());
    }
}
