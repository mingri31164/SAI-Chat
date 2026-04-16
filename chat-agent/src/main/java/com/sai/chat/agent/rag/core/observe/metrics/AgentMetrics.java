/*
 * Agent 核心指标
 */

package com.sai.chat.agent.rag.core.observe.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent 核心指标
 */
public class AgentMetrics {

    // ==================== 计数器 ====================

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong timeoutRequests = new AtomicLong(0);
    private final AtomicLong llmCalls = new AtomicLong(0);
    private final AtomicLong toolCalls = new AtomicLong(0);
    private final AtomicLong totalTokens = new AtomicLong(0);
    private final AtomicLong promptTokens = new AtomicLong(0);
    private final AtomicLong completionTokens = new AtomicLong(0);

    // ==================== 累计值 ====================

    private final AtomicLong totalDurationMs = new AtomicLong(0);
    private final AtomicLong llmDurationMs = new AtomicLong(0);
    private final AtomicLong toolDurationMs = new AtomicLong(0);
    private final AtomicLong totalCostCents = new AtomicLong(0);

    // ==================== 滑动窗口统计 ====================

    private final ConcurrentHashMap<String, SlidingWindow> latencyWindows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TaggedCounter> taggedCounters = new ConcurrentHashMap<>();

    public AgentMetrics() {
    }

    // ==================== 指标记录方法 ====================

    public void incrementRequest() {
        totalRequests.incrementAndGet();
    }

    public void incrementSuccess() {
        successRequests.incrementAndGet();
    }

    public void incrementFailed() {
        failedRequests.incrementAndGet();
    }

    public void incrementTimeout() {
        timeoutRequests.incrementAndGet();
    }

    public void incrementLlmCalls() {
        llmCalls.incrementAndGet();
    }

    public void incrementToolCalls() {
        toolCalls.incrementAndGet();
    }

    public void incrementToolCalls(int count) {
        toolCalls.addAndGet(count);
    }

    public void recordTokens(long prompt, long completion) {
        promptTokens.addAndGet(prompt);
        completionTokens.addAndGet(completion);
        totalTokens.addAndGet(prompt + completion);
    }

    public void recordDuration(long durationMs) {
        totalDurationMs.addAndGet(durationMs);
    }

    public void recordLlmDuration(long durationMs) {
        llmDurationMs.addAndGet(durationMs);
    }

    public void recordToolDuration(long durationMs) {
        toolDurationMs.addAndGet(durationMs);
    }

    public void recordCost(long costCents) {
        totalCostCents.addAndGet(costCents);
    }

    public void recordLatency(String windowKey, long latencyMs) {
        SlidingWindow window = latencyWindows.computeIfAbsent(windowKey, k -> new SlidingWindow(1000));
        window.record(latencyMs);
    }

    public void incrementTagged(String tag, String value) {
        String key = tag + ":" + value;
        TaggedCounter counter = taggedCounters.computeIfAbsent(key, k -> new TaggedCounter(tag, value));
        counter.increment();
    }

    // ==================== 指标查询方法 ====================

    public AtomicLong getTotalRequests() {
        return totalRequests;
    }

    public AtomicLong getSuccessRequests() {
        return successRequests;
    }

    public AtomicLong getFailedRequests() {
        return failedRequests;
    }

    public AtomicLong getTimeoutRequests() {
        return timeoutRequests;
    }

    public AtomicLong getLlmCalls() {
        return llmCalls;
    }

    public AtomicLong getToolCalls() {
        return toolCalls;
    }

    public AtomicLong getTotalTokens() {
        return totalTokens;
    }

    public AtomicLong getTotalDurationMs() {
        return totalDurationMs;
    }

    public AtomicLong getLlmDurationMs() {
        return llmDurationMs;
    }

    public AtomicLong getToolDurationMs() {
        return toolDurationMs;
    }

    public AtomicLong getTotalCostCents() {
        return totalCostCents;
    }

    public double getSuccessRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) successRequests.get() / total : 0.0;
    }

    public double getErrorRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) failedRequests.get() / total : 0.0;
    }

    public double getTimeoutRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) timeoutRequests.get() / total : 0.0;
    }

    public double getAverageDurationMs() {
        long total = totalRequests.get();
        return total > 0 ? (double) totalDurationMs.get() / total : 0.0;
    }

    public double getAverageLlmDurationMs() {
        long calls = llmCalls.get();
        return calls > 0 ? (double) llmDurationMs.get() / calls : 0.0;
    }

    public double getAverageToolDurationMs() {
        long calls = toolCalls.get();
        return calls > 0 ? (double) toolDurationMs.get() / calls : 0.0;
    }

    public double getAverageTokensPerRequest() {
        long total = totalRequests.get();
        return total > 0 ? (double) totalTokens.get() / total : 0.0;
    }

    public double getQps() {
        SlidingWindow window = latencyWindows.get("qps");
        return window != null ? window.getQps() : 0.0;
    }

    public double getP99LatencyMs() {
        SlidingWindow window = latencyWindows.get("duration");
        return window != null ? window.getP99() : 0.0;
    }

    public double getP95LatencyMs() {
        SlidingWindow window = latencyWindows.get("duration");
        return window != null ? window.getP95() : 0.0;
    }

    public double getP50LatencyMs() {
        SlidingWindow window = latencyWindows.get("duration");
        return window != null ? window.getP50() : 0.0;
    }

    public double getTotalCostYuan() {
        return totalCostCents.get() / 100.0;
    }

    // ==================== 内部类 ====================

    public static class SlidingWindow {
        private static final int DEFAULT_SIZE = 1000;
        private final long[] values;
        private final int size;
        private volatile int index;
        private volatile int count;
        private volatile long lastTimestamp;
        private volatile long windowStartTime;
        private volatile double cachedQps;

        public SlidingWindow(int size) {
            this.size = size > 0 ? size : DEFAULT_SIZE;
            this.values = new long[this.size];
            this.windowStartTime = System.currentTimeMillis();
        }

        public synchronized void record(long value) {
            values[index] = value;
            index = (index + 1) % size;
            if (count < size) count++;
            lastTimestamp = System.currentTimeMillis();

            if (count % 100 == 0) {
                long windowDuration = lastTimestamp - windowStartTime;
                if (windowDuration > 0) {
                    cachedQps = (double) count / windowDuration * 1000;
                }
            }
        }

        public double getP50() {
            return percentile(0.5);
        }

        public double getP95() {
            return percentile(0.95);
        }

        public double getP99() {
            return percentile(0.99);
        }

        public double getQps() {
            return cachedQps;
        }

        private double percentile(double p) {
            if (count == 0) return 0;
            long[] sorted = new long[count];
            for (int i = 0; i < count; i++) {
                sorted[i] = values[i];
            }
            java.util.Arrays.sort(sorted);
            int pos = (int) Math.ceil(p * count) - 1;
            pos = Math.max(0, Math.min(pos, count - 1));
            return sorted[pos];
        }

        public int getCount() {
            return count;
        }
    }

    public static class TaggedCounter {
        private final String tag;
        private final String value;
        private final AtomicLong count = new AtomicLong(0);

        public TaggedCounter(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        public void increment() {
            count.incrementAndGet();
        }

        public long getCount() {
            return count.get();
        }
    }
}
