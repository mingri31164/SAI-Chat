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
 * Unless required by applicable law or agreed in in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sai.chat.agent.rag.core.retrieval.channel;

import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.SearchChannelResult;
import com.sai.chat.agent.framework.convention.rag.SearchContext;
import com.sai.chat.agent.framework.convention.rag.enums.SearchChannelType;
import com.sai.chat.agent.infra.embedding.EmbeddingService;
import com.sai.chat.agent.infra.vector.MilvusProperties;
import com.sai.chat.agent.infra.vector.VectorStoreService;
import com.sai.chat.agent.rag.config.RAGProperties;
import com.sai.chat.agent.rag.constant.RAGConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * 向量全局检索通道
 * <p>
 * 不依赖意图识别，直接在默认 Collection 中做向量相似度检索。
 * 作为兜底策略使用，当意图置信度不足时触发。
 * <p>
 * 特点：
 * - 不依赖 IntentNode，无偏检索
 * - 覆盖所有知识库（跨 Collection 并行）
 * - 适合泛化问题（无法确定具体意图时）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorGlobalSearchChannel implements SearchChannel {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final RAGProperties ragProperties;
    private final MilvusProperties milvusProperties;

    @Override
    public SearchChannelResult search(SearchContext ctx, IntentNode node, int topK) {
        long start = System.currentTimeMillis();

        try {
            String question = ctx.getMainQuestion();
            int effectiveTopK = computeTopK(topK);
            String collection = milvusProperties.getCollection().getDefaultName();

            // 1. 问题向量化
            List<Float> embeddingVector = embeddingService.embed(question);
            float[] queryVector = toFloatArray(embeddingVector);

            // 2. 向量检索
            var chunks = vectorStoreService.search(collection, queryVector, effectiveTopK);

            double confidence = computeConfidence(chunks, effectiveTopK);
            long latency = System.currentTimeMillis() - start;

            log.debug("向量全局检索完成, question={}, collection={}, topK={}, results={}, latency={}ms",
                    question, collection, effectiveTopK, chunks.size(), latency);

            return SearchChannelResult.builder()
                    .channelType(SearchChannelType.VECTOR_GLOBAL)
                    .channelName("vector-global")
                    .chunks(chunks)
                    .confidence(confidence)
                    .latencyMs(latency)
                    .metadata(new HashMap<>())
                    .build();

        } catch (Exception e) {
            log.warn("向量全局检索失败, question={}", ctx.getMainQuestion(), e);
            return SearchChannelResult.builder()
                    .channelType(SearchChannelType.VECTOR_GLOBAL)
                    .channelName("vector-global")
                    .chunks(List.of())
                    .confidence(0.0)
                    .latencyMs(System.currentTimeMillis() - start)
                    .metadata(new HashMap<>())
                    .build();
        }
    }

    @Override
    public boolean isAvailable() {
        return ragProperties.getSearch().getVectorGlobal().isEnabled();
    }

    private int computeTopK(int requestedTopK) {
        int multiplier = ragProperties.getSearch().getVectorGlobal().getTopKMultiplier();
        int computed = Math.max(requestedTopK * multiplier, RAGConstant.MIN_SEARCH_TOP_K);
        return computed;
    }

    private double computeConfidence(List<?> chunks, int topK) {
        if (chunks == null || chunks.isEmpty()) {
            return 0.0;
        }
        return Math.min(1.0, (double) chunks.size() / topK);
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
