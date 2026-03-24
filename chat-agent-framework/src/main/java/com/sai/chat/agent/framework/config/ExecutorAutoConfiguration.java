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

package com.sai.chat.agent.framework.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RAG 场景专用线程池配置
 * <p>
 * 为系统中不同的业务场景配置独立的线程池，提高并发处理能力并实现资源隔离：
 * <ul>
 *   <li>MCP 批处理：CPU 密集型工具并行执行</li>
 *   <li>RAG 检索：向量搜索/关键词检索并行通道</li>
 *   <li>RAG 内部检索：Collection 内多批次并行</li>
 *   <li>意图识别：多意图并行 LLM 分类</li>
 *   <li>会话摘要：LLM 摘要异步生成</li>
 *   <li>流式输出：LLM 流式推理</li>
 *   <li>SSE 排队执行：SSE 入口排队控制</li>
 *   <li>知识库分块：文档解析与分块</li>
 * </ul>
 * <p>
 * 所有线程池均通过 TTL 包装，保证 TransmittableThreadLocal 在异步线程中正常透传
 */
@Configuration
public class ExecutorAutoConfiguration {

    /**
     * CPU 核心数，用于动态计算线程池大小
     */
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * MCP 批处理线程池（CPU 密集型）
     * 用于 MCP 工具并行调用，每个工具独立执行
     */
    @Bean
    public Executor mcpBatchThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT << 1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryBuilder.create().setNamePrefix("mcp_batch_executor_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * RAG 上下文处理线程池（轻量）
     * 用于对话摘要、上下文组装等轻量异步任务
     */
    @Bean
    public Executor ragContextThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                4,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryBuilder.create().setNamePrefix("rag_context_executor_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * RAG 检索线程池（用于通道级别的并行）
     * 用于多路检索引擎中各 SearchChannel 并行执行
     */
    @Bean
    public Executor ragRetrievalThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT << 1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryBuilder.create().setNamePrefix("rag_retrieval_executor_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * RAG 内部检索线程池（高吞吐）
     * 用于单个 Collection 内部多批次并行检索，有队列缓冲
     */
    @Bean
    public Executor ragInnerRetrievalThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CPU_COUNT << 1,
                CPU_COUNT << 2,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                ThreadFactoryBuilder.create().setNamePrefix("rag_inner_retrieval_executor_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * 意图识别并行执行线程池
     * 用于多意图并行 LLM 分类
     */
    @Bean
    public Executor intentClassifyThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT << 1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryBuilder.create().setNamePrefix("intent_classify_executor_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * 会话记忆摘要生成线程池（单线程保守）
     * 用于 LLM 异步生成对话摘要，防止频繁触发
     */
    @Bean
    public Executor memorySummaryThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                Math.max(2, CPU_COUNT >> 1),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                ThreadFactoryBuilder.create().setNamePrefix("memory_summary_executor_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * 模型流式输出线程池
     * 用于 LLM 流式推理，避免阻塞业务线程
     */
    @Bean
    public Executor modelStreamExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                Math.max(2, CPU_COUNT >> 1),
                Math.max(4, CPU_COUNT),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                ThreadFactoryBuilder.create().setNamePrefix("model_stream_executor_").build(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * SSE 排队后执行入口线程池
     * 用于 SSE 入口排队控制，防止并发过载
     */
    @Bean
    public Executor chatEntryExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                Math.max(2, CPU_COUNT >> 1),
                Math.max(4, CPU_COUNT),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                ThreadFactoryBuilder.create().setNamePrefix("chat_entry_executor_").build(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * 知识库文档分块线程池
     * 用于文档解析与分块处理
     */
    @Bean
    public Executor knowledgeChunkExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                Math.max(2, CPU_COUNT >> 1),
                Math.max(4, CPU_COUNT),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                ThreadFactoryBuilder.create().setNamePrefix("kb_chunk_executor_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }
}
