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

package com.sai.chat.agent.rag.core.observe.trace;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TraceContext 持有者 - 线程本地存储
 * <p>
 * 使用 ThreadLocal 确保每个线程有独立的追踪上下文，
 * 支持异步执行链路追踪。
 */
@Slf4j
public class TraceContextHolder {

    private static final ThreadLocal<TraceContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    /**
     * 获取当前追踪上下文
     */
    public static TraceContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 设置追踪上下文
     */
    public static void setContext(TraceContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 清除追踪上下文
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 创建新的追踪上下文
     */
    public static TraceContext createNew(String sessionId, String userId) {
        TraceContext context = TraceContext.builder()
                .traceId(generateTraceId())
                .sessionId(sessionId)
                .userId(userId)
                .startTimeMs(System.currentTimeMillis())
                .build();
        setContext(context);
        return context;
    }

    /**
     * 生成追踪ID
     */
    private static String generateTraceId() {
        return "trace-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 创建跨度
     */
    public static AgentTrace.Span createSpan(String name, AgentTrace.Span.SpanType type) {
        TraceContext context = getContext();
        if (context == null) {
            log.warn("No TraceContext found, creating new one");
            context = createNew(null, null);
        }
        return context.createSpan(name, type);
    }

    /**
     * 创建子跨度
     */
    public static AgentTrace.Span createChildSpan(String name, AgentTrace.Span.SpanType type, String parentSpanId) {
        TraceContext context = getContext();
        if (context == null) {
            log.warn("No TraceContext found");
            return null;
        }
        return context.createChildSpan(name, type, parentSpanId);
    }

    /**
     * 添加追踪事件
     */
    public static void addEvent(String name, AgentTrace.TraceEvent.EventType type) {
        TraceContext context = getContext();
        if (context != null) {
            context.addEvent(name, type);
        }
    }

    /**
     * 记录错误
     */
    public static void recordError(String message, Throwable error) {
        TraceContext context = getContext();
        if (context != null) {
            context.recordError(message, error);
        }
    }

    /**
     * 获取追踪ID
     */
    public static String getTraceId() {
        TraceContext context = getContext();
        return context != null ? context.getTraceId() : null;
    }

    /**
     * 检查是否在追踪上下文中
     */
    public static boolean isInContext() {
        return getContext() != null;
    }

    /**
     * 追踪上下文 - 内部类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TraceContext {
        private String traceId;
        private String sessionId;
        private String userId;
        private long startTimeMs;

        @lombok.Builder.Default
        private Map<String, AgentTrace.Span> spanMap = new ConcurrentHashMap<>();

        @lombok.Builder.Default
        private Map<String, AgentTrace.TraceEvent> eventMap = new ConcurrentHashMap<>();

        private AgentTrace.Span rootSpan;
        private AgentTrace.Span currentSpan;
        private String lastError;
        private Throwable lastErrorThrowable;

        /**
         * 创建根跨度
         */
        public AgentTrace.Span createRootSpan(String name, AgentTrace.Span.SpanType type) {
            String spanId = generateSpanId();
            AgentTrace.Span span = AgentTrace.Span.builder()
                    .spanId(spanId)
                    .parentSpanId(null)
                    .traceId(this.traceId)
                    .name(name)
                    .type(type)
                    .startTimeMs(System.currentTimeMillis())
                    .status(AgentTrace.Span.SpanStatus.STARTED)
                    .build();
            this.spanMap.put(spanId, span);
            this.rootSpan = span;
            this.currentSpan = span;
            return span;
        }

        /**
         * 创建跨度
         */
        public AgentTrace.Span createSpan(String name, AgentTrace.Span.SpanType type) {
            String parentSpanId = currentSpan != null ? currentSpan.getSpanId() : null;
            return createChildSpan(name, type, parentSpanId);
        }

        /**
         * 创建子跨度
         */
        public AgentTrace.Span createChildSpan(String name, AgentTrace.Span.SpanType type, String parentSpanId) {
            String spanId = generateSpanId();
            AgentTrace.Span span = AgentTrace.Span.builder()
                    .spanId(spanId)
                    .parentSpanId(parentSpanId)
                    .traceId(this.traceId)
                    .name(name)
                    .type(type)
                    .startTimeMs(System.currentTimeMillis())
                    .status(AgentTrace.Span.SpanStatus.STARTED)
                    .build();
            this.spanMap.put(spanId, span);

            if (parentSpanId != null && this.spanMap.containsKey(parentSpanId)) {
                this.spanMap.get(parentSpanId).addChild(span);
            }

            this.currentSpan = span;
            return span;
        }

        /**
         * 结束当前跨度
         */
        public void endCurrentSpan(AgentTrace.Span.SpanStatus status) {
            if (currentSpan != null) {
                currentSpan.setStatus(status);
                currentSpan.complete();
                currentSpan = currentSpan.getParentSpanId() != null
                        ? this.spanMap.get(currentSpan.getParentSpanId())
                        : null;
            }
        }

        /**
         * 结束当前跨度(带错误)
         */
        public void endCurrentSpanWithError(AgentTrace.Span.SpanStatus status, String error) {
            if (currentSpan != null) {
                currentSpan.setStatus(status);
                currentSpan.setError(error);
                currentSpan.complete();
                currentSpan = currentSpan.getParentSpanId() != null
                        ? this.spanMap.get(currentSpan.getParentSpanId())
                        : null;
            }
        }

        /**
         * 添加事件
         */
        public void addEvent(String name, AgentTrace.TraceEvent.EventType type) {
            String eventId = generateEventId();
            AgentTrace.TraceEvent event = AgentTrace.TraceEvent.builder()
                    .eventId(eventId)
                    .timestampMs(System.currentTimeMillis())
                    .name(name)
                    .type(type)
                    .spanId(currentSpan != null ? currentSpan.getSpanId() : null)
                    .build();
            this.eventMap.put(eventId, event);
        }

        /**
         * 记录错误
         */
        public void recordError(String message, Throwable error) {
            this.lastError = message;
            this.lastErrorThrowable = error;
            if (currentSpan != null) {
                currentSpan.setError(message);
            }
        }

        /**
         * 转换为AgentTrace
         */
        public AgentTrace toAgentTrace() {
            return AgentTrace.builder()
                    .traceId(this.traceId)
                    .sessionId(this.sessionId)
                    .userId(this.userId)
                    .startTimeMs(this.startTimeMs)
                    .endTimeMs(System.currentTimeMillis())
                    .totalDurationMs(System.currentTimeMillis() - this.startTimeMs)
                    .status(this.lastError != null
                            ? AgentTrace.TraceStatus.FAILED
                            : AgentTrace.TraceStatus.SUCCESS)
                    .spanList(this.spanMap.values().stream().filter(s -> s.getParentSpanId() == null).toList())
                    .events(this.eventMap.values().stream().toList())
                    .errorMessage(this.lastError)
                    .errorStack(this.lastErrorThrowable != null
                            ? getStackTrace(this.lastErrorThrowable)
                            : null)
                    .build();
        }

        private String generateSpanId() {
            return "span-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        private String generateEventId() {
            return "evt-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        private String getStackTrace(Throwable t) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : t.getStackTrace()) {
                sb.append(element.toString()).append("\n");
                if (sb.length() > 1000) {
                    sb.append("... (truncated)");
                    break;
                }
            }
            return sb.toString();
        }
    }
}
