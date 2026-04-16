/*
 * 观测API端点
 */

package com.sai.chat.agent.rag.core.observe.metrics;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.rag.core.observe.trace.AgentTrace;
import com.sai.chat.agent.rag.core.observe.trace.TraceQueryService;
import com.sai.chat.agent.rag.core.observe.trace.TraceRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 观测API端点
 */
@RestController
@RequestMapping("/api/observe")
@RequiredArgsConstructor
public class ObserveController {

    private final MetricsCollector metricsCollector;
    private final TraceQueryService traceQueryService;
    private final TraceRecorder traceRecorder;

    @GetMapping("/metrics/snapshot")
    public Result<MetricsCollector.MetricsSnapshot> getMetricsSnapshot() {
        MetricsCollector.MetricsSnapshot snapshot = metricsCollector.getGlobalSnapshot();
        return new Result<MetricsCollector.MetricsSnapshot>().setCode(Result.SUCCESS_CODE).setData(snapshot);
    }

    @GetMapping("/metrics/tools")
    public Result<Map<String, Object>> getToolMetrics() {
        Map<String, MetricsCollector.ToolMetrics> toolMetricsMap = metricsCollector.getAllToolMetrics();
        Map<String, Object> result = new java.util.HashMap<>();
        for (Map.Entry<String, MetricsCollector.ToolMetrics> entry : toolMetricsMap.entrySet()) {
            MetricsCollector.ToolMetrics m = entry.getValue();
            Map<String, Object> toolInfo = Map.of(
                    "toolId", m.getToolId(),
                    "toolName", m.getToolName(),
                    "totalCalls", m.getTotalCalls().get(),
                    "successCalls", m.getSuccessCalls().get(),
                    "failedCalls", m.getFailedCalls().get(),
                    "successRate", m.getSuccessRate(),
                    "averageDurationMs", m.getAverageDurationMs(),
                    "minDurationMs", m.getMinDurationMs().get(),
                    "maxDurationMs", m.getMaxDurationMs().get()
            );
            result.put(entry.getKey(), toolInfo);
        }
        return new Result<Map<String, Object>>().setCode(Result.SUCCESS_CODE).setData(result);
    }

    @GetMapping("/metrics/session/{sessionId}")
    public Result<MetricsCollector.SessionMetrics> getSessionMetrics(@PathVariable String sessionId) {
        MetricsCollector.SessionMetrics sessionMetrics = metricsCollector.getSessionMetrics(sessionId);
        return new Result<MetricsCollector.SessionMetrics>().setCode(Result.SUCCESS_CODE).setData(sessionMetrics);
    }

    @GetMapping("/trace/{traceId}")
    public Result<String> getTraceDetail(@PathVariable String traceId) {
        String detail = traceQueryService.getTraceDetail(traceId);
        return new Result<String>().setCode(Result.SUCCESS_CODE).setData(detail);
    }

    @GetMapping("/trace/session/{sessionId}")
    public Result<List<AgentTrace>> getSessionTraces(@PathVariable String sessionId) {
        List<AgentTrace> traces = traceQueryService.getSessionTraces(sessionId);
        return new Result<List<AgentTrace>>().setCode(Result.SUCCESS_CODE).setData(traces);
    }

    @GetMapping("/trace/recent")
    public Result<List<AgentTrace>> getRecentTraces(@RequestParam(defaultValue = "20") int limit) {
        List<AgentTrace> traces = traceQueryService.getRecentTraces(limit);
        return new Result<List<AgentTrace>>().setCode(Result.SUCCESS_CODE).setData(traces);
    }

    @GetMapping("/trace/failed")
    public Result<List<AgentTrace>> getFailedTraces() {
        List<AgentTrace> traces = traceQueryService.getFailedTraces();
        return new Result<List<AgentTrace>>().setCode(Result.SUCCESS_CODE).setData(traces);
    }

    @GetMapping("/trace/{traceId}/latency")
    public Result<TraceQueryService.LatencyAnalysis> getLatencyAnalysis(@PathVariable String traceId) {
        TraceQueryService.LatencyAnalysis analysis = traceQueryService.analyzeLatency(traceId);
        return new Result<TraceQueryService.LatencyAnalysis>().setCode(Result.SUCCESS_CODE).setData(analysis);
    }

    @PostMapping("/trace/cleanup")
    public Result<Void> cleanupOldTraces(@RequestParam(defaultValue = "24") long ttlHours) {
        traceQueryService.cleanupOldTraces(ttlHours);
        return new Result<Void>().setCode(Result.SUCCESS_CODE);
    }

    @PostMapping("/metrics/report")
    public Result<Void> triggerReport() {
        metricsCollector.reportNow();
        return new Result<Void>().setCode(Result.SUCCESS_CODE);
    }

    @PostMapping("/metrics/reset")
    public Result<Void> resetMetrics() {
        metricsCollector.reset();
        return new Result<Void>().setCode(Result.SUCCESS_CODE);
    }

    @GetMapping("/trace/statistics")
    public Result<TraceRecorder.TraceStatistics> getTraceStatistics() {
        TraceRecorder.TraceStatistics stats = traceRecorder.getStatistics();
        return new Result<TraceRecorder.TraceStatistics>().setCode(Result.SUCCESS_CODE).setData(stats);
    }
}
