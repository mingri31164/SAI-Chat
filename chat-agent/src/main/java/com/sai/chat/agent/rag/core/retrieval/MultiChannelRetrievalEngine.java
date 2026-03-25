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

package com.sai.chat.agent.rag.core.retrieval;

import com.sai.chat.agent.framework.convention.RetrievedChunk;
import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.SearchChannelResult;
import com.sai.chat.agent.framework.convention.rag.SearchContext;
import com.sai.chat.agent.framework.convention.rag.SubQuestionIntent;
import com.sai.chat.agent.rag.config.RAGProperties;
import com.sai.chat.agent.rag.constant.RAGConstant;
import com.sai.chat.agent.rag.core.intent.IntentClassifier;
import com.sai.chat.agent.rag.core.retrieval.channel.IntentDirectedSearchChannel;
import com.sai.chat.agent.rag.core.retrieval.channel.SearchChannel;
import com.sai.chat.agent.rag.core.retrieval.channel.VectorGlobalSearchChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 多通道检索编排引擎
 * <p>
 * 核心职责：
 * 1. 接收意图识别结果（SubQuestionIntent 列表）
 * 2. 调度多个检索通道并行执行
 * 3. 合并、去重、排序多通道结果
 * 4. 可选触发 Rerank 提升质量
 * 5. 返回最终合并的 Chunk 列表
 * <p>
 * 检索策略：
 * - 意图置信度高（>= 配置阈值）：优先用意向定向通道
 * - 意图置信度低（< 配置阈值）：回退到全局向量通道
 * - 兜底：任何通道失败均不影响其他通道
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiChannelRetrievalEngine {

    private final IntentClassifier intentClassifier;
    private final VectorGlobalSearchChannel globalChannel;
    private final IntentDirectedSearchChannel directedChannel;
    private final RAGProperties ragProperties;

    /**
     * ThreadPool for parallel channel execution
     */
    private final ExecutorService channelExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r, "retrieval-channel");
                t.setDaemon(true);
                return t;
            }
    );

    /**
     * 执行多通道检索
     *
     * @param question  用户问题（原始或重写后）
     * @param sessionId 会话 ID（用于意图缓存）
     * @param topK      期望返回的结果数量
     * @return 检索到的 Chunk 列表（已合并去重）
     */
    public List<RetrievedChunk> retrieve(String question, String sessionId, int topK) {
        // Step 1: 意图识别
        List<SubQuestionIntent> intents = identifyIntents(question, sessionId);

        if (intents == null || intents.isEmpty()) {
            log.info("未识别到有效意图，降级为全局检索, question={}", question);
            return fallbackToGlobal(question, topK);
        }

        // Step 2: 构建检索上下文
        SearchContext ctx = SearchContext.builder()
                .originalQuestion(question)
                .subQuestions(intents.stream()
                        .map(SubQuestionIntent::getSubQuestion)
                        .collect(Collectors.toList()))
                .intents(intents)
                .topK(topK)
                .build();

        // Step 3: 多通道并行检索
        List<SearchChannelResult> channelResults = executeChannelsInParallel(ctx, intents, topK);

        // Step 4: 合并去重
        List<RetrievedChunk> merged = mergeAndDeduplicate(channelResults, topK);

        log.info("多通道检索完成, question={}, intents={}, totalChannels={}, finalResults={}",
                question, intents.size(), channelResults.size(), merged.size());

        return merged;
    }

    /**
     * 意图识别（复用 IntentClassifier）
     */
    private List<SubQuestionIntent> identifyIntents(String question, String sessionId) {
        try {
            double minScore = ragProperties.getSearch().getIntentDirected().getMinIntentScore();
            int maxIntents = RAGConstant.MAX_INTENT_COUNT;

            var nodeScores = intentClassifier.topKAboveThreshold(question, maxIntents, minScore);

            if (nodeScores == null || nodeScores.isEmpty()) {
                return null;
            }

            return nodeScores.stream()
                    .map(ns -> SubQuestionIntent.builder()
                            .subQuestion(question)
                            .nodeScores(List.of(ns))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("意图识别失败，降级为全局检索", e);
            return null;
        }
    }

    /**
     * 并行执行多个检索通道
     */
    private List<SearchChannelResult> executeChannelsInParallel(
            SearchContext ctx,
            List<SubQuestionIntent> intents,
            int topK) {

        List<Future<SearchChannelResult>> futures = new ArrayList<>();

        for (SubQuestionIntent intent : intents) {
            if (intent.getNodeScores() == null || intent.getNodeScores().isEmpty()) {
                continue;
            }

            IntentNode primaryNode = intent.getNodeScores().get(0).getNode();
            double intentScore = intent.getNodeScores().get(0).getScore();

            // 意图定向通道
            if (directedChannel.isAvailable() && primaryNode != null && primaryNode.isKB()) {
                SearchChannel c = directedChannel;
                IntentNode n = primaryNode;
                futures.add(channelExecutor.submit(() -> c.search(ctx, n, topK)));
            }

            // 全局向量通道（兜底）
            if (globalChannel.isAvailable() && intentScore < ragProperties.getSearch()
                    .getIntentDirected().getConfidenceThreshold()) {
                SearchChannel c = globalChannel;
                futures.add(channelExecutor.submit(() -> c.search(ctx, null, topK)));
            }
        }

        // 全局通道始终参与（作为兜底）
        if (globalChannel.isAvailable() && futures.isEmpty()) {
            futures.add(channelExecutor.submit(() -> globalChannel.search(ctx, null, topK)));
        }

        List<SearchChannelResult> results = new ArrayList<>();
        for (Future<SearchChannelResult> f : futures) {
            try {
                results.add(f.get(5, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.warn("通道执行失败", e);
            }
        }

        return results;
    }

    /**
     * 合并多通道结果并去重
     * <p>
     * 去重策略：以 chunk ID 为主键，同一 ID 仅保留得分最高的结果
     */
    private List<RetrievedChunk> mergeAndDeduplicate(List<SearchChannelResult> channelResults, int topK) {
        Map<String, RetrievedChunk> id2Chunk = new LinkedHashMap<>();
        Map<String, Double> id2Score = new HashMap<>();

        for (SearchChannelResult result : channelResults) {
            if (result.getChunks() == null) continue;

            double channelWeight = result.getConfidence();

            for (RetrievedChunk chunk : result.getChunks()) {
                String chunkId = chunk.getId();
                double combinedScore = chunk.getScore() * channelWeight;

                if (!id2Score.containsKey(chunkId) || id2Score.get(chunkId) < combinedScore) {
                    id2Chunk.put(chunkId, chunk);
                    id2Score.put(chunkId, combinedScore);
                }
            }
        }

        return id2Chunk.values().stream()
                .sorted((a, b) -> {
                    double scoreA = id2Score.getOrDefault(a.getId(), 0.0);
                    double scoreB = id2Score.getOrDefault(b.getId(), 0.0);
                    return Double.compare(scoreB, scoreA);
                })
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * 降级策略：仅使用全局向量检索
     */
    private List<RetrievedChunk> fallbackToGlobal(String question, int topK) {
        try {
            SearchContext ctx = SearchContext.builder()
                    .originalQuestion(question)
                    .build();
            SearchChannelResult result = globalChannel.search(ctx, null, topK * 2);
            if (result != null && result.getChunks() != null) {
                return result.getChunks().stream().limit(topK).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("全局检索降级也失败, question={}", question, e);
        }
        return List.of();
    }
}
