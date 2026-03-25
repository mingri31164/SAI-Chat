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

package com.sai.chat.agent.rag.core.retrieval.channel;

import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.SearchChannelResult;
import com.sai.chat.agent.framework.convention.rag.SearchContext;
import com.sai.chat.agent.framework.convention.rag.enums.IntentKind;
import com.sai.chat.agent.framework.convention.rag.enums.SearchChannelType;
import com.sai.chat.agent.infra.embedding.EmbeddingService;
import com.sai.chat.agent.infra.vector.VectorStoreService;
import com.sai.chat.agent.rag.config.RAGProperties;
import com.sai.chat.agent.rag.constant.RAGConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * 意图定向检索通道
 * <p>
 * 基于意图识别结果，精准定位到对应知识库的 Collection 进行向量检索。
 * <p>
 * 特点：
 * - 依赖 IntentNode 的 collectionName
 * - 精准定位，单 Collection 检索，召回率高
 * - 意图置信度越高，此通道效果越好
 * - 支持 KB（向量检索）和 MCP（工具调用）两种模式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentDirectedSearchChannel implements SearchChannel {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final RAGProperties ragProperties;

    @Override
    public SearchChannelResult search(SearchContext ctx, IntentNode node, int topK) {
        long start = System.currentTimeMillis();

        if (node == null || node.getCollectionName() == null || node.getCollectionName().isBlank()) {
            log.debug("意图节点无 Collection 信息，跳过意图定向检索");
            return emptyResult(ctx, start, "no-collection");
        }

        if (!node.isKB()) {
            log.debug("意图类型为 {}，非 KB 跳过向量检索", node.getKind());
            return emptyResult(ctx, start, "non-kb-kind");
        }

        try {
            String question = ctx.getMainQuestion();
            int effectiveTopK = computeTopK(topK, node);
            String collection = node.getCollectionName();

            // 1. 问题向量化
            List<Float> embeddingVector = embeddingService.embed(question);
            float[] queryVector = toFloatArray(embeddingVector);

            // 2. 在指定 Collection 中检索
            var chunks = vectorStoreService.search(collection, queryVector, effectiveTopK);

            double confidence = computeChunkConfidence(chunks);
            long latency = System.currentTimeMillis() - start;

            log.debug("意图定向检索完成, question={}, collection={}, topK={}, results={}, latency={}ms",
                    question, collection, effectiveTopK, chunks.size(), latency);

            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("intentNodeId", node.getId());
            metadata.put("intentNodeName", node.getName());
            metadata.put("kind", node.getKind().name());

            return SearchChannelResult.builder()
                    .channelType(SearchChannelType.INTENT_DIRECTED)
                    .channelName("intent-directed:" + node.getName())
                    .chunks(chunks)
                    .confidence(confidence)
                    .latencyMs(latency)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.warn("意图定向检索失败, node={}", node.getId(), e);
            return SearchChannelResult.builder()
                    .channelType(SearchChannelType.INTENT_DIRECTED)
                    .channelName("intent-directed:" + node.getName())
                    .chunks(List.of())
                    .confidence(0.0)
                    .latencyMs(System.currentTimeMillis() - start)
                    .metadata(new HashMap<>())
                    .build();
        }
    }

    @Override
    public boolean isAvailable() {
        return ragProperties.getSearch().getIntentDirected().isEnabled();
    }

    private int computeTopK(int requestedTopK, IntentNode node) {
        int multiplier = ragProperties.getSearch().getIntentDirected().getTopKMultiplier();
        int nodeTopK = node.getTopK() != null ? node.getTopK() : requestedTopK;
        int computed = Math.max(nodeTopK * multiplier, RAGConstant.MIN_SEARCH_TOP_K);
        return computed;
    }

    private double computeChunkConfidence(List<?> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return 0.0;
        }
        return 0.6;
    }

    private SearchChannelResult emptyResult(SearchContext ctx, long start, String reason) {
        return SearchChannelResult.builder()
                .channelType(SearchChannelType.INTENT_DIRECTED)
                .channelName("intent-directed:empty")
                .chunks(List.of())
                .confidence(0.0)
                .latencyMs(System.currentTimeMillis() - start)
                .metadata(new HashMap<>())
                .build();
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
