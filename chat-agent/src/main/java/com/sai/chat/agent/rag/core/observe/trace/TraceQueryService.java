/*
 * 追踪查询服务
 */

package com.sai.chat.agent.rag.core.observe.trace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 追踪查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceQueryService {

    private final TraceRecorder traceRecorder;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String getTraceDetail(String traceId) {
        return traceRecorder.getByTraceId(traceId)
                .map(traceRecorder::exportToJson)
                .orElse("{\"error\": \"Trace not found: " + traceId + "\"}");
    }

    public List<AgentTrace> getSessionTraces(String sessionId) {
        return traceRecorder.getBySessionId(sessionId);
    }

    public List<AgentTrace> getRecentTraces(int limit) {
        return traceRecorder.getRecentTraces(limit);
    }

    public List<AgentTrace> getFailedTraces() {
        return traceRecorder.getFailedTraces();
    }

    public TraceRecorder.TraceStatistics getStatistics() {
        return traceRecorder.getStatistics();
    }

    public void cleanupOldTraces(long ttlHours) {
        traceRecorder.cleanupOldTraces(ttlHours * 60 * 60 * 1000L);
    }

    public LatencyAnalysis analyzeLatency(String traceId) {
        return traceRecorder.getByTraceId(traceId)
                .map(this::buildLatencyAnalysis)
                .orElse(null);
    }

    private LatencyAnalysis buildLatencyAnalysis(AgentTrace trace) {
        long totalDuration = trace.getTotalDurationMs();
        List<AgentTrace.Span> spans = trace.getSpanList();

        long llmTime = 0;
        long toolTime = 0;
        long otherTime = 0;

        for (AgentTrace.Span span : spans) {
            if (span.getType() == AgentTrace.Span.SpanType.LLM_REASONING) {
                llmTime += span.getDurationMs();
            } else if (span.getType() == AgentTrace.Span.SpanType.TOOL_CALL) {
                toolTime += span.getDurationMs();
            } else {
                otherTime += span.getDurationMs();
            }
        }

        return LatencyAnalysis.builder()
                .traceId(trace.getTraceId())
                .totalDurationMs(totalDuration)
                .llmDurationMs(llmTime)
                .toolDurationMs(toolTime)
                .otherDurationMs(otherTime)
                .llmPercentage(totalDuration > 0 ? (double) llmTime / totalDuration * 100 : 0)
                .toolPercentage(totalDuration > 0 ? (double) toolTime / totalDuration * 100 : 0)
                .spanCount(spans.size())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class LatencyAnalysis {
        private String traceId;
        private long totalDurationMs;
        private long llmDurationMs;
        private long toolDurationMs;
        private long otherDurationMs;
        private double llmPercentage;
        private double toolPercentage;
        private int spanCount;
    }
}
