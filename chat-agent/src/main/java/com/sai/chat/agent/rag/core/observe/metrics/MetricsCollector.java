/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sai.chat.agent.rag.core.observe.metrics;

import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.observe.trace.AgentTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 指标采集器
 * <p>
 * 核心组件，负责收集、聚合和上报Agent运行指标：
 * <ul>
 *   <li>实时指标采集</li>
 *   <li>历史指标统计</li>
 *   <li>指标快照导出</li>
 *   <li>定时上报</li>
 * </ul>
 */
@Slf4j
@Component
public class MetricsCollector {

    /**
     * 全局指标实例
     */
    private final AgentMetrics globalMetrics;

    /**
     * 按会话聚合的指标
     */
    private final ConcurrentHashMap<String, SessionMetrics> sessionMetrics = new ConcurrentHashMap<>();

    /**
     * 按工具聚合的指标
     */
    private final ConcurrentHashMap<String, ToolMetrics> toolMetrics = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 上报处理器列表
     */
    private final List<MetricsReporter> reporters = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * 上报间隔(秒)
     */
    private static final long REPORT_INTERVAL_SECONDS = 60;

    public MetricsCollector() {
        this.globalMetrics = new AgentMetrics();
        startPeriodicReport();
    }

    // ==================== 指标记录接口 ====================

    /**
     * 记录请求开始
     */
    public void recordRequestStart(String sessionId, String userId) {
        globalMetrics.incrementRequest();
        globalMetrics.incrementTagged("user_id", userId != null ? userId : "anonymous");

        SessionMetrics session = sessionMetrics.computeIfAbsent(sessionId, SessionMetrics::new);
        session.incrementRequest();
    }

    /**
     * 记录请求完成
     */
    public void recordRequestEnd(String sessionId, AgentResponse response, long durationMs) {
        // 全局指标
        globalMetrics.recordDuration(durationMs);
        globalMetrics.recordLatency("duration", durationMs);

        if (response.isSuccess()) {
            globalMetrics.incrementSuccess();
            globalMetrics.incrementTagged("status", "success");
        } else {
            globalMetrics.incrementFailed();
            globalMetrics.incrementTagged("status", "failed");

            if (response.getErrorMessage() != null) {
                globalMetrics.incrementTagged("error_type", classifyError(response.getErrorMessage()));
            }
        }

        // 会话指标
        SessionMetrics session = sessionMetrics.get(sessionId);
        if (session != null) {
            session.recordDuration(durationMs, response.isSuccess());
        }

        // Token消耗
        if (response.getTotalTokens() > 0) {
            globalMetrics.recordTokens(response.getTotalTokens(), 0);
            globalMetrics.incrementTagged("token_range", classifyTokenRange(response.getTotalTokens()));
        }

        // 工具调用统计
        if (response.getToolCallCount() > 0) {
            globalMetrics.incrementToolCalls(response.getToolCallCount());
            globalMetrics.incrementTagged("tool_call_count", classifyToolCallCount(response.getToolCallCount()));
        }
    }

    /**
     * 记录LLM调用
     */
    public void recordLlmCall(String model, long promptTokens, long completionTokens,
                             long durationMs, double cost) {
        globalMetrics.incrementLlmCalls();
        globalMetrics.recordLlmDuration(durationMs);
        globalMetrics.recordTokens(promptTokens, completionTokens);
        globalMetrics.recordCost((long) (cost * 100)); // 转换为分

        globalMetrics.incrementTagged("llm_model", model);
    }

    /**
     * 记录工具调用
     */
    public void recordToolCall(String toolId, String toolName, boolean success, long durationMs) {
        ToolMetrics tool = toolMetrics.computeIfAbsent(toolId, k -> new ToolMetrics(toolId, toolName));
        tool.recordCall(success, durationMs);

        if (success) {
            globalMetrics.incrementTagged("tool", toolName + "_success");
        } else {
            globalMetrics.incrementTagged("tool", toolName + "_failed");
        }
    }

    /**
     * 记录追踪
     */
    public void recordTrace(AgentTrace trace) {
        // 从追踪中提取指标
        if (trace.getSpanList() != null) {
            for (AgentTrace.Span span : trace.getSpanList()) {
                if (span.getType() == AgentTrace.Span.SpanType.LLM_REASONING) {
                    globalMetrics.incrementLlmCalls();
                    if (span.getDurationMs() > 0) {
                        globalMetrics.recordLlmDuration(span.getDurationMs());
                    }
                } else if (span.getType() == AgentTrace.Span.SpanType.TOOL_CALL) {
                    globalMetrics.incrementToolCalls();
                    if (span.getDurationMs() > 0) {
                        globalMetrics.recordToolDuration(span.getDurationMs());
                    }
                }
            }
        }

        // 迭代次数
        int iterationCount = trace.getSpanCount();
        globalMetrics.incrementTagged("iteration_range", classifyIterationRange(iterationCount));
    }

    /**
     * 记录超时
     */
    public void recordTimeout(String sessionId) {
        globalMetrics.incrementTimeout();
        globalMetrics.incrementTagged("status", "timeout");

        SessionMetrics session = sessionMetrics.get(sessionId);
        if (session != null) {
            session.recordTimeout();
        }
    }

    // ==================== 指标查询接口 ====================

    /**
     * 获取全局指标快照
     */
    public MetricsSnapshot getGlobalSnapshot() {
        return MetricsSnapshot.builder()
                .timestamp(System.currentTimeMillis())
                .totalRequests(globalMetrics.getTotalRequests().get())
                .successRequests(globalMetrics.getSuccessRequests().get())
                .failedRequests(globalMetrics.getFailedRequests().get())
                .timeoutRequests(globalMetrics.getTimeoutRequests().get())
                .successRate(globalMetrics.getSuccessRate())
                .errorRate(globalMetrics.getErrorRate())
                .timeoutRate(globalMetrics.getTimeoutRate())
                .averageDurationMs(globalMetrics.getAverageDurationMs())
                .p50LatencyMs(globalMetrics.getP50LatencyMs())
                .p95LatencyMs(globalMetrics.getP95LatencyMs())
                .p99LatencyMs(globalMetrics.getP99LatencyMs())
                .qps(globalMetrics.getQps())
                .llmCalls(globalMetrics.getLlmCalls().get())
                .averageLlmDurationMs(globalMetrics.getAverageLlmDurationMs())
                .toolCalls(globalMetrics.getToolCalls().get())
                .averageToolDurationMs(globalMetrics.getAverageToolDurationMs())
                .totalTokens(globalMetrics.getTotalTokens().get())
                .averageTokensPerRequest(globalMetrics.getAverageTokensPerRequest())
                .totalCostYuan(globalMetrics.getTotalCostYuan())
                .activeSessions(sessionMetrics.size())
                .build();
    }

    /**
     * 获取会话指标
     */
    public SessionMetrics getSessionMetrics(String sessionId) {
        return sessionMetrics.get(sessionId);
    }

    /**
     * 获取工具指标
     */
    public ToolMetrics getToolMetrics(String toolId) {
        return toolMetrics.get(toolId);
    }

    /**
     * 获取所有工具指标
     */
    public Map<String, ToolMetrics> getAllToolMetrics() {
        return new ConcurrentHashMap<>(toolMetrics);
    }

    /**
     * 获取所有会话指标
     */
    public Map<String, SessionMetrics> getAllSessionMetrics() {
        return new ConcurrentHashMap<>(sessionMetrics);
    }

    // ==================== 上报管理 ====================

    /**
     * 添加上报处理器
     */
    public void addReporter(MetricsReporter reporter) {
        reporters.add(reporter);
    }

    /**
     * 移除上报处理器
     */
    public void removeReporter(MetricsReporter reporter) {
        reporters.remove(reporter);
    }

    /**
     * 触发立即上报
     */
    public void reportNow() {
        MetricsSnapshot snapshot = getGlobalSnapshot();
        for (MetricsReporter reporter : reporters) {
            try {
                reporter.report(snapshot);
            } catch (Exception e) {
                log.warn("Metrics reporter failed: {}", reporter.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 清理会话指标
     */
    public void cleanupSession(String sessionId) {
        sessionMetrics.remove(sessionId);
    }

    /**
     * 重置所有指标
     */
    public void reset() {
        globalMetrics.getTotalRequests().set(0);
        globalMetrics.getSuccessRequests().set(0);
        globalMetrics.getFailedRequests().set(0);
        globalMetrics.getTimeoutRequests().set(0);
        globalMetrics.getTotalDurationMs().set(0);
        globalMetrics.getLlmCalls().set(0);
        globalMetrics.getLlmDurationMs().set(0);
        globalMetrics.getToolCalls().set(0);
        globalMetrics.getToolDurationMs().set(0);
        globalMetrics.getTotalTokens().set(0);
        globalMetrics.getTotalCostCents().set(0);
        sessionMetrics.clear();
        toolMetrics.clear();
    }

    // ==================== 私有方法 ====================

    private void startPeriodicReport() {
        scheduler.scheduleAtFixedRate(this::reportNow,
                REPORT_INTERVAL_SECONDS,
                REPORT_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    private String classifyError(String error) {
        if (error == null) return "unknown";
        if (error.contains("timeout")) return "timeout";
        if (error.contains("connection")) return "connection";
        if (error.contains("auth") || error.contains("permission")) return "auth";
        if (error.contains("rate")) return "rate_limit";
        if (error.contains("llm") || error.contains("model")) return "llm";
        if (error.contains("tool")) return "tool";
        return "other";
    }

    private String classifyTokenRange(long tokens) {
        if (tokens < 100) return "tiny";
        if (tokens < 500) return "small";
        if (tokens < 2000) return "medium";
        if (tokens < 8000) return "large";
        return "huge";
    }

    private String classifyToolCallCount(int count) {
        if (count == 0) return "zero";
        if (count == 1) return "single";
        if (count <= 3) return "few";
        if (count <= 10) return "many";
        return "excessive";
    }

    private String classifyIterationRange(int iterations) {
        if (iterations <= 1) return "direct";
        if (iterations <= 3) return "few";
        if (iterations <= 5) return "moderate";
        if (iterations <= 10) return "many";
        return "excessive";
    }

    // ==================== 内部类 ====================

    /**
     * 会话指标
     */
    @lombok.Data
    public static class SessionMetrics {
        private final String sessionId;
        private final java.util.concurrent.atomic.AtomicLong requestCount = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong successCount = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong failedCount = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong timeoutCount = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalDurationMs = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalTokens = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalIterations = new java.util.concurrent.atomic.AtomicLong(0);
        private volatile long firstRequestTime;
        private volatile long lastRequestTime;

        public SessionMetrics(String sessionId) {
            this.sessionId = sessionId;
        }

        public void incrementRequest() {
            requestCount.incrementAndGet();
            lastRequestTime = System.currentTimeMillis();
            if (firstRequestTime == 0) firstRequestTime = lastRequestTime;
        }

        public void recordDuration(long durationMs, boolean success) {
            totalDurationMs.addAndGet(durationMs);
            if (success) {
                successCount.incrementAndGet();
            } else {
                failedCount.incrementAndGet();
            }
        }

        public void recordTimeout() {
            timeoutCount.incrementAndGet();
        }

        public void recordTokens(long tokens) {
            totalTokens.addAndGet(tokens);
        }

        public void recordIterations(int iterations) {
            totalIterations.addAndGet(iterations);
        }

        public double getAverageDurationMs() {
            long total = requestCount.get();
            return total > 0 ? (double) totalDurationMs.get() / total : 0;
        }

        public double getSuccessRate() {
            long total = requestCount.get();
            return total > 0 ? (double) successCount.get() / total : 0;
        }
    }

    /**
     * 工具指标
     */
    @lombok.Data
    public static class ToolMetrics {
        private final String toolId;
        private final String toolName;
        private final java.util.concurrent.atomic.AtomicLong totalCalls = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong successCalls = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong failedCalls = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalDurationMs = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong minDurationMs = new java.util.concurrent.atomic.AtomicLong(Long.MAX_VALUE);
        private final java.util.concurrent.atomic.AtomicLong maxDurationMs = new java.util.concurrent.atomic.AtomicLong(0);

        public ToolMetrics(String toolId, String toolName) {
            this.toolId = toolId;
            this.toolName = toolName;
        }

        public void recordCall(boolean success, long durationMs) {
            totalCalls.incrementAndGet();
            totalDurationMs.addAndGet(durationMs);

            if (success) {
                successCalls.incrementAndGet();
            } else {
                failedCalls.incrementAndGet();
            }

            // 更新min/max
            long currentMin = minDurationMs.get();
            if (durationMs < currentMin) {
                minDurationMs.compareAndSet(currentMin, durationMs);
            }

            long currentMax = maxDurationMs.get();
            if (durationMs > currentMax) {
                maxDurationMs.compareAndSet(currentMax, durationMs);
            }
        }

        public double getSuccessRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) successCalls.get() / total : 0;
        }

        public double getAverageDurationMs() {
            long total = totalCalls.get();
            return total > 0 ? (double) totalDurationMs.get() / total : 0;
        }
    }

    /**
     * 指标快照
     */
    @lombok.Data
    @lombok.Builder
    public static class MetricsSnapshot {
        private long timestamp;
        private long totalRequests;
        private long successRequests;
        private long failedRequests;
        private long timeoutRequests;
        private double successRate;
        private double errorRate;
        private double timeoutRate;
        private double averageDurationMs;
        private double p50LatencyMs;
        private double p95LatencyMs;
        private double p99LatencyMs;
        private double qps;
        private long llmCalls;
        private double averageLlmDurationMs;
        private long toolCalls;
        private double averageToolDurationMs;
        private long totalTokens;
        private double averageTokensPerRequest;
        private double totalCostYuan;
        private int activeSessions;
    }

    /**
     * 指标上报接口
     */
    public interface MetricsReporter {
        void report(MetricsSnapshot snapshot);
    }
}
