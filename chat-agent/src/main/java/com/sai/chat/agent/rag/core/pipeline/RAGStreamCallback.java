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

package com.sai.chat.agent.rag.core.pipeline;

import com.sai.chat.agent.infra.chat.StreamCallback;
import com.sai.chat.agent.framework.web.SseEmitterSender;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * RAG 流式回调适配器
 * <p>
 * 将 {@link StreamCallback} 的增量内容通过 SSE 实时推送给客户端，
 * 并通过 CompletableFuture 提供完成状态查询
 */
@Slf4j
public class RAGStreamCallback implements StreamCallback {

    private final SseEmitterSender sender;
    private final CompletableFuture<String> completionFuture = new CompletableFuture<>();
    private final StringBuilder fullContent = new StringBuilder();

    public RAGStreamCallback(SseEmitterSender sender) {
        this.sender = sender;
    }

    @Override
    public void onContent(String content) {
        try {
            fullContent.append(content);
            sender.sendEvent(SSEventType.ANSWER, content);
        } catch (Exception e) {
            log.debug("SSE 内容推送失败，可能客户端已断开", e);
        }
    }

    @Override
    public void onThinking(String content) {
        try {
            sender.sendEvent(SSEventType.THINKING, content);
        } catch (Exception e) {
            log.debug("SSE thinking 推送失败", e);
        }
    }

    @Override
    public void onComplete() {
        try {
            sender.sendEvent(SSEventType.DONE, fullContent.toString());
            sender.complete();
            completionFuture.complete(fullContent.toString());
        } catch (Exception e) {
            log.debug("SSE 完成推送失败", e);
            completionFuture.completeExceptionally(e);
        }
    }

    @Override
    public void onError(Throwable error) {
        try {
            sender.sendEvent(SSEventType.ERROR, error.getMessage());
            sender.fail(error);
            completionFuture.completeExceptionally(error);
        } catch (Exception e) {
            log.debug("SSE 错误推送失败", e);
        }
    }

    /**
     * 等待流式回复完成
     *
     * @return 最终完整回复内容
     * @throws Exception 如果流式回复异常
     */
    public String awaitCompletion() throws Exception {
        return completionFuture.join();
    }

    public String getFullContent() {
        return fullContent.toString();
    }
}
