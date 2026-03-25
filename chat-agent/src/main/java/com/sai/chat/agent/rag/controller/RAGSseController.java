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

package com.sai.chat.agent.rag.controller;

import com.sai.chat.agent.infra.chat.StreamCancellationHandle;
import com.sai.chat.agent.framework.web.SseEmitterSender;
import com.sai.chat.agent.rag.core.pipeline.RAGPipelineServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * RAG SSE 流式对话控制器
 * <p>
 * 提供 SSE 流式接口，支持：
 * - 完整的 RAG 链路流式输出
 * - 分阶段推送：意图分类 → 查询改写 → 检索结果 → LLM 回复流
 * - 客户端可随时通过断开连接取消推理
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RAGSseController {

    private final RAGPipelineServiceImpl ragPipelineService;

    /**
     * SSE 流式 RAG 对话接口
     * <p>
     * 返回类型: text/event-stream
     * <p>
     * 推送事件顺序：
     * <pre>
     * 1. intent:  意图分类结果 {"scores": [...], "topScore": 0.85}
     * 2. rewrite: 查询改写结果 {"rewrittenQuestion": "...", "subQuestions": [...]}
     * 3. retrieval: 检索结果 {"count": 5, "chunks": [...]}
     * 4. answer:  LLM 回复内容（每个 token 推送一次）
     * 5. done:    回复完成 {"fullContent": "..."}
     * </pre>
     * <p>
     * 异常时推送：
     * <pre>
     * error: {"message": "错误描述"}
     * </pre>
     *
     * @param question     用户问题
     * @param sessionId   会话 ID
     * @param deepThinking 是否启用深度思考（LLM 思考过程）
     * @return SseEmitter Spring SSE 发送器
     */
    @GetMapping(value = "/chat/sse", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatSse(
            @RequestParam String question,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false, defaultValue = "false") Boolean deepThinking) {

        log.info("SSE RAG 对话请求, question={}, sessionId={}, deepThinking={}",
                question, sessionId, deepThinking);

        SseEmitter emitter = new SseEmitter(0L);
        SseEmitterSender sender = new SseEmitterSender(emitter);

        ragPipelineService.streamChat(
                question,
                sessionId != null ? sessionId : "",
                Boolean.TRUE.equals(deepThinking),
                sender
        );

        return emitter;
    }

    /**
     * 健康检查端点（用于测试 SSE 连接）
     */
    @GetMapping(value = "/sse/health", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter sseHealth() {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("{\"status\":\"ok\"}"));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
