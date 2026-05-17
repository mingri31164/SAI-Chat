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

package com.sai.chat.agent.infra.vector;

import com.sai.chat.agent.framework.convention.RetrievedChunk;

import java.util.List;

/**
 * 向量存储服务接口
 * <p>
 * 定义文档向量索引的写入、更新、删除操作
 * <p>
 * 支持 pgvector（默认）和 Milvus（通过 {@code agent.milvus.enabled=true} 启用）
 */
public interface VectorStoreService {

    /**
     * 批量建立文档的向量索引
     *
     * @param collection Collection 名称
     * @param docId     文档唯一标识
     * @param chunks    文档切片列表（每条包含文本内容和预计算的向量）
     */
    void indexDocumentChunks(String collection, String docId, List<VectorChunk> chunks);

    /**
     * 更新单个 chunk 的向量索引（Upsert）
     *
     * @param collection Collection 名称
     * @param docId      文档唯一标识
     * @param chunk      待更新的文档切片
     */
    void updateChunk(String collection, String docId, VectorChunk chunk);

    /**
     * 删除文档的所有向量索引
     *
     * @param collection Collection 名称
     * @param docId      文档唯一标识
     */
    void deleteDocumentVectors(String collection, String docId);

    /**
     * 删除指定的单个 chunk 向量索引
     *
     * @param collection Collection 名称
     * @param chunkId    chunk 的唯一标识
     */
    void deleteChunkById(String collection, String chunkId);

    /**
     * 根据向量检索相似 Chunk
     *
     * @param collection  Collection 名称
     * @param queryVector 查询向量
     * @param topK        返回的最相似结果数量
     * @return 检索结果列表
     */
    List<RetrievedChunk> search(String collection, float[] queryVector, int topK);

    /**
     * 根据向量检索相似 Chunk（带额外过滤条件）
     *
     * @param collection Collection 名称
     * @param queryVector 查询向量
     * @param topK       返回的最相似结果数量
     * @param filterExpr 过滤表达式（pgvector 格式，如 metadata->>'kb_id' = 'xxx'）
     * @return 检索结果列表
     */
    List<RetrievedChunk> searchWithFilter(String collection, float[] queryVector, int topK, String filterExpr);
}
