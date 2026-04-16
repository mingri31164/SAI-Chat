/*
 * 追踪记录器
 */

package com.sai.chat.agent.rag.core.observe.trace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 追踪记录器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceRecorder {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .create();

    private final List<AgentTrace> traceStore = new java.util.concurrent.CopyOnWriteArrayList<>();

    private static final int MAX_STORE_SIZE = 1000;

    public void record(AgentTrace trace) {
        try {
            addToStore(trace);
            logToFile(trace);
            for (Consumer<AgentTrace> handler : getHandlers()) {
                try {
                    handler.accept(trace);
                } catch (Exception e) {
                    log.warn("Trace handler failed: {}", e.getMessage());
                }
            }
            log.debug("Trace recorded: traceId={}, status={}, duration={}ms, spans={}, events={}",
                    trace.getTraceId(), trace.getStatus(), trace.getTotalDurationMs(),
                    trace.getSpanCount(), trace.getEventCount());
        } catch (Exception e) {
            log.error("Failed to record trace: {}", e.getMessage(), e);
        }
    }

    public Optional<AgentTrace> getByTraceId(String traceId) {
        return traceStore.stream().filter(t -> t.getTraceId().equals(traceId)).findFirst();
    }

    public List<AgentTrace> getBySessionId(String sessionId) {
        return traceStore.stream().filter(t -> sessionId.equals(t.getSessionId())).toList();
    }

    public List<AgentTrace> getRecentTraces(int limit) {
        return traceStore.stream()
                .sorted((a, b) -> Long.compare(b.getStartTimeMs(), a.getStartTimeMs()))
                .limit(limit)
                .toList();
    }

    public List<AgentTrace> getFailedTraces() {
        return traceStore.stream().filter(t -> t.getStatus() == AgentTrace.TraceStatus.FAILED).toList();
    }

    public void cleanupOldTraces(long olderThanMs) {
        long cutoffTime = System.currentTimeMillis() - olderThanMs;
        traceStore.removeIf(t -> t.getStartTimeMs() < cutoffTime);
        log.info("Cleaned up traces older than {}ms, remaining: {}", olderThanMs, traceStore.size());
    }

    public TraceStatistics getStatistics() {
        int total = traceStore.size();
        int success = (int) traceStore.stream()
                .filter(t -> t.getStatus() == AgentTrace.TraceStatus.SUCCESS).count();
        int failed = (int) traceStore.stream()
                .filter(t -> t.getStatus() == AgentTrace.TraceStatus.FAILED).count();
        double avgDuration = total > 0 ? traceStore.stream()
                .mapToLong(AgentTrace::getTotalDurationMs)
                .average().orElse(0) : 0;

        return TraceStatistics.builder()
                .totalTraces(total)
                .successCount(success)
                .failedCount(failed)
                .successRate(total > 0 ? (double) success / total : 0.0)
                .averageDurationMs((long) avgDuration)
                .build();
    }

    public String exportToJson(AgentTrace trace) {
        return gson.toJson(trace);
    }

    public AgentTrace importFromJson(String json) {
        return gson.fromJson(json, AgentTrace.class);
    }

    private void addToStore(AgentTrace trace) {
        traceStore.add(trace);
        while (traceStore.size() > MAX_STORE_SIZE) {
            traceStore.remove(0);
        }
    }

    private void logToFile(AgentTrace trace) {
        log.info("[TRACE] {}|{}|{}|{}ms|{}|{}|{}",
                trace.getTraceId(), trace.getSessionId(), trace.getStatus(),
                trace.getTotalDurationMs(), trace.getSpanCount(), trace.getEventCount(),
                trace.getErrorMessage() != null ? trace.getErrorMessage() : "");
    }

    private List<Consumer<AgentTrace>> getHandlers() {
        return List.of();
    }

    @lombok.Data
    @lombok.Builder
    public static class TraceStatistics {
        private int totalTraces;
        private int successCount;
        private int failedCount;
        private double successRate;
        private long averageDurationMs;
    }
}
