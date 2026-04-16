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

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 结构化追踪上下文
 * <p>
 * 完整的追踪链路，包含：
 * <ul>
 *   <li>traceId - 全局唯一追踪ID</li>
 *   <li>spanList - 追踪跨度列表</li>
 *   <li>events - 关键事件列表</li>
 *   <li>metadata - 元数据</li>
 * </ul>
 */
@Data
@Builder
public class AgentTrace {

    /**
     * 全局追踪ID
     */
    private String traceId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 追踪开始时间
     */
    private long startTimeMs;

    /**
     * 追踪结束时间
     */
    private long endTimeMs;

    /**
     * 总耗时(ms)
     */
    private long totalDurationMs;

    /**
     * 最终状态
     */
    private TraceStatus status;

    /**
     * 追踪跨度列表
     */
    @Builder.Default
    private List<Span> spanList = new ArrayList<>();

    /**
     * 追踪事件列表
     */
    @Builder.Default
    private List<TraceEvent> events = new ArrayList<>();

    /**
     * 元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new ConcurrentHashMap<>();

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误堆栈
     */
    private String errorStack;

    /**
     * 添加跨度
     */
    public void addSpan(Span span) {
        this.spanList.add(span);
    }

    /**
     * 添加事件
     */
    public void addEvent(TraceEvent event) {
        this.events.add(event);
    }

    /**
     * 添加元数据
     */
    public void putMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * 获取元数据
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    /**
     * 完成追踪
     */
    public void complete(TraceStatus status) {
        this.endTimeMs = System.currentTimeMillis();
        this.totalDurationMs = this.endTimeMs - this.startTimeMs;
        this.status = status;
    }

    /**
     * 获取跨度数量
     */
    public int getSpanCount() {
        return spanList != null ? spanList.size() : 0;
    }

    /**
     * 获取事件数量
     */
    public int getEventCount() {
        return events != null ? events.size() : 0;
    }

    /**
     * 追踪状态枚举
     */
    public enum TraceStatus {
        /**
         * 成功完成
         */
        SUCCESS,
        /**
         * 执行中
         */
        RUNNING,
        /**
         * 执行失败
         */
        FAILED,
        /**
         * 执行超时
         */
        TIMEOUT,
        /**
         * 达到最大迭代
         */
        MAX_ITERATIONS_EXCEEDED,
        /**
         * 用户取消
         */
        CANCELLED,
        /**
         * 未知状态
         */
        UNKNOWN
    }

    /**
     * 追踪跨度
     */
    @Data
    @Builder
    public static class Span {
        /**
         * 跨度ID
         */
        private String spanId;

        /**
         * 父跨度ID
         */
        private String parentSpanId;

        /**
         * 追踪ID
         */
        private String traceId;

        /**
         * 跨度名称
         */
        private String name;

        /**
         * 跨度类型
         */
        private SpanType type;

        /**
         * 开始时间
         */
        private long startTimeMs;

        /**
         * 结束时间
         */
        private long endTimeMs;

        /**
         * 耗时(ms)
         */
        private long durationMs;

        /**
         * 状态
         */
        private SpanStatus status;

        /**
         * 输入数据(JSON字符串)
         */
        private String input;

        /**
         * 输出数据(JSON字符串)
         */
        private String output;

        /**
         * 错误信息
         */
        private String error;

        /**
         * 跨度属性
         */
        @Builder.Default
        private Map<String, String> attributes = new ConcurrentHashMap<>();

        /**
         * 子跨度
         */
        @Builder.Default
        private List<Span> children = new ArrayList<>();

        /**
         * 完成跨度
         */
        public void complete() {
            this.endTimeMs = System.currentTimeMillis();
            this.durationMs = this.endTimeMs - this.startTimeMs;
        }

        /**
         * 添加子跨度
         */
        public void addChild(Span child) {
            this.children.add(child);
        }

        /**
         * 添加属性
         */
        public void setAttribute(String key, String value) {
            this.attributes.put(key, value);
        }

        /**
         * 跨度类型
         */
        public enum SpanType {
            /**
             * LLM推理
             */
            LLM_REASONING,
            /**
             * 工具调用
             */
            TOOL_CALL,
            /**
             * 反思评估
             */
            REFLECTION,
            /**
             * 记忆检索
             */
            MEMORY_RETRIEVAL,
            /**
             * 上下文构建
             */
            CONTEXT_BUILDING,
            /**
             * 响应生成
             */
            RESPONSE_GENERATION,
            /**
             * 意图分类
             */
            INTENT_CLASSIFICATION,
            /**
             * 查询改写
             */
            QUERY_REWRITE,
            /**
             * 检索
             */
            RETRIEVAL,
            /**
             * 重排序
             */
            RERANK,
            /**
             * 其他
             */
            OTHER
        }

        /**
         * 跨度状态
         */
        public enum SpanStatus {
            /**
             * 开始
             */
            STARTED,
            /**
             * 运行中
             */
            RUNNING,
            /**
             * 成功
             */
            SUCCESS,
            /**
             * 失败
             */
            FAILED,
            /**
             * 超时
             */
            TIMEOUT
        }
    }

    /**
     * 追踪事件
     */
    @Data
    @Builder
    public static class TraceEvent {
        /**
         * 事件ID
         */
        private String eventId;

        /**
         * 事件时间
         */
        private long timestampMs;

        /**
         * 事件名称
         */
        private String name;

        /**
         * 事件类型
         */
        private EventType type;

        /**
         * 关联跨度ID
         */
        private String spanId;

        /**
         * 事件内容
         */
        private String content;

        /**
         * 事件数据
         */
        @Builder.Default
        private Map<String, Object> data = new ConcurrentHashMap<>();

        /**
         * 事件类型枚举
         */
        public enum EventType {
            /**
             * Agent启动
             */
            AGENT_STARTED,
            /**
             * Agent完成
             */
            AGENT_COMPLETED,
            /**
             * Agent失败
             */
            AGENT_FAILED,
            /**
             * 迭代开始
             */
            ITERATION_STARTED,
            /**
             * 迭代结束
             */
            ITERATION_COMPLETED,
            /**
             * 思考开始
             */
            THINKING_STARTED,
            /**
             * 思考结束
             */
            THINKING_COMPLETED,
            /**
             * 工具调用开始
             */
            TOOL_CALL_STARTED,
            /**
             * 工具调用结束
             */
            TOOL_CALL_COMPLETED,
            /**
             * 反思开始
             */
            REFLECTION_STARTED,
            /**
             * 反思结束
             */
            REFLECTION_COMPLETED,
            /**
             * 状态变更
             */
            STATUS_CHANGED,
            /**
             * 错误发生
             */
            ERROR_OCCURRED,
            /**
             * 用户交互
             */
            USER_INTERACTION,
            /**
             * 系统事件
             */
            SYSTEM
        }
    }
}
