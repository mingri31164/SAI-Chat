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

package com.sai.chat.agent.rag.core.retrieval;

import com.sai.chat.agent.framework.convention.RetrievedChunk;

import java.util.List;

/**
 * 检索结果重排序服务接口
 * <p>
 * 负责将多通道检索得到的候选 Chunk 按与问题的语义相关性重新排序。
 * 典型实现：
 * - 基于 Cross-Encoder 的重排序（精度高，速度慢）
 * - 基于 Embedding + Cosine 的轻量重排（速度快，精度次之）
 * - 仅返回 TopN（不改变顺序，仅截断）
 */
public interface RerankService {

    /**
     * 对候选 Chunk 进行重排序
     *
     * @param question  用户问题
     * @param chunks   候选 Chunk 列表（来自多通道检索的结果）
     * @param topN     最终返回的结果数量
     * @return 重排序后的 Chunk 列表
     */
    List<RetrievedChunk> rerank(String question, List<RetrievedChunk> chunks, int topN);
}
