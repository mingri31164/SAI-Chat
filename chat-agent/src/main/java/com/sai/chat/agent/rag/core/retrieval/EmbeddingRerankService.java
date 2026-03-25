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
import com.sai.chat.agent.infra.embedding.EmbeddingService;
import com.sai.chat.agent.rag.config.RAGProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于 Embedding Cosine 的轻量级重排序实现
 * <p>
 * 策略：
 * 1. 对每个候选 Chunk，将"问题 + Chunk 内容"拼接后向量化
 * 2. 计算问题向量与各 Chunk 向量的余弦相似度
 * 3. 结合原始相似度分数进行加权排序
 * <p>
 * 特点：
 * - 速度较快（可批量化），适合作为兜底重排
 * - 如需更高精度，可替换为 Cross-Encoder 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingRerankService implements RerankService {

    private final EmbeddingService embeddingService;
    private final RAGProperties ragProperties;

    private static final float RERANK_WEIGHT = 0.6F;
    private static final float ORIGINAL_WEIGHT = 0.4F;

    @Override
    public List<RetrievedChunk> rerank(String question, List<RetrievedChunk> chunks, int topN) {
        if (chunks == null || chunks.isEmpty()) {
            return List.of();
        }

        if (chunks.size() <= topN) {
            return new ArrayList<>(chunks);
        }

        try {
            List<Float> questionVector = embeddingService.embed(question);
            float[] qVec = toFloatArray(questionVector);

            // 批量构建 (chunkId, prompt, chunk) 元组
            List<RerankCandidate> candidates = new ArrayList<>();
            for (RetrievedChunk chunk : chunks) {
                String prompt = buildPrompt(question, chunk.getText());
                candidates.add(new RerankCandidate(chunk.getId(), prompt, chunk));
            }

            // 批量向量化
            List<String> texts = candidates.stream().map(RerankCandidate::prompt).collect(Collectors.toList());
            List<List<Float>> vectors = embeddingService.embedBatch(texts);

            // 计算余弦相似度并加权排序
            List<Float> questionNorm = normalize(qVec);
            List<ScoredChunk> scored = new ArrayList<>();

            for (int i = 0; i < candidates.size(); i++) {
                RetrievedChunk chunk = candidates.get(i).chunk();
                List<Float> chunkVec = vectors.get(i);
                float[] cVec = toFloatArray(chunkVec);

                float cosineSim = cosineSimilarity(questionNorm, normalize(cVec));

                // 原始相似度分数归一化（原始分数通常在 0-1 之间）
                float normalizedOriginal = (float) Math.min(1.0, Math.max(0.0, chunk.getScore()));

                // 加权综合分数
                float combinedScore = RERANK_WEIGHT * cosineSim + ORIGINAL_WEIGHT * normalizedOriginal;

                scored.add(new ScoredChunk(chunk, combinedScore));
            }

            return scored.stream()
                    .sorted((a, b) -> Float.compare(b.score, a.score))
                    .limit(topN)
                    .map(ScoredChunk::chunk)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("重排序失败，回退使用原始顺序", e);
            return chunks.stream().limit(topN).collect(Collectors.toList());
        }
    }

    private String buildPrompt(String question, String chunkText) {
        return "问题：" + question + "\n\n相关段落：" + chunkText;
    }

    private float cosineSimilarity(List<Float> a, List<Float> b) {
        if (a.size() != b.size()) return 0f;
        float dot = 0f, normA = 0f, normB = 0f;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        float denom = (float) Math.sqrt(normA) * (float) Math.sqrt(normB);
        return denom == 0f ? 0f : dot / denom;
    }

    private List<Float> normalize(float[] vec) {
        float norm = 0f;
        for (float v : vec) norm += v * v;
        norm = (float) Math.sqrt(norm);
        if (norm == 0f) return new ArrayList<>();
        List<Float> result = new ArrayList<>(vec.length);
        for (float v : vec) result.add(v / norm);
        return result;
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private record RerankCandidate(String chunkId, String prompt, RetrievedChunk chunk) {}
    private record ScoredChunk(RetrievedChunk chunk, float score) {}
}
