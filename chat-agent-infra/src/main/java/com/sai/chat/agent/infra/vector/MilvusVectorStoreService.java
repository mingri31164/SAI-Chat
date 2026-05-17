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

import cn.hutool.core.lang.Assert;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mingri.toolkit.SnowflakeIdUtil;
import com.sai.chat.agent.framework.convention.RetrievedChunk;
import com.sai.chat.agent.framework.exception.ClientException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Milvus 向量存储服务实现
 * <p>
 * 实现 {@link VectorStoreService} 接口，封装 Milvus SDK v2 的增删改查操作
 * <p>
 * 默认禁用，如需启用请设置 {@code agent.milvus.enabled=true}
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agent", name = "milvus.enabled", havingValue = "true", matchIfMissing = false)
public class MilvusVectorStoreService implements VectorStoreService {

    private final MilvusClientV2 milvusClient;
    private final MilvusProperties milvusProperties;

    @Override
    public void indexDocumentChunks(String collection, String docId, List<VectorChunk> chunks) {
        Assert.notEmpty(chunks, () -> new ClientException("文档分块不允许为空"));
        int dimension = resolveDimension(collection);

        List<JsonObject> rows = buildChunkRows(chunks, docId, collection, dimension);

        InsertReq req = InsertReq.builder()
                .collectionName(collection)
                .data(rows)
                .build();

        InsertResp resp = milvusClient.insert(req);
        log.info("Milvus 写入向量索引成功, collection={}, docId={}, rows={}", collection, docId, resp.getInsertCnt());
    }

    @Override
    public void updateChunk(String collection, String docId, VectorChunk chunk) {
        Assert.notNull(chunk, () -> new ClientException("Chunk 对象不能为空"));
        int dimension = resolveDimension(collection);
            String chunkPk = chunk.getChunkId() != null ? chunk.getChunkId() : SnowflakeIdUtil.nextIdStr();

        List<JsonObject> rows = buildSingleChunkRow(chunk, docId, chunkPk, collection, dimension);

        UpsertReq upsertReq = UpsertReq.builder()
                .collectionName(collection)
                .data(rows)
                .build();

        UpsertResp resp = milvusClient.upsert(upsertReq);
        log.info("Milvus 更新向量索引成功, collection={}, docId={}, chunkId={}, upsertCnt={}",
                collection, docId, chunkPk, resp.getUpsertCnt());
    }

    @Override
    public void deleteDocumentVectors(String collection, String docId) {
        String filter = "metadata[\"doc_id\"] == \"" + docId + "\"";
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(collection)
                .filter(filter)
                .build();

        DeleteResp resp = milvusClient.delete(deleteReq);
        log.info("Milvus 删除文档向量成功, collection={}, docId={}, deleteCnt={}",
                collection, docId, resp.getDeleteCnt());
    }

    @Override
    public void deleteChunkById(String collection, String chunkId) {
        String filter = "doc_id == \"" + chunkId + "\"";
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(collection)
                .filter(filter)
                .build();

        DeleteResp resp = milvusClient.delete(deleteReq);
        log.info("Milvus 删除 Chunk 成功, collection={}, chunkId={}, deleteCnt={}",
                collection, chunkId, resp.getDeleteCnt());
    }

    @Override
    public List<RetrievedChunk> search(String collection, float[] queryVector, int topK) {
        return doSearch(collection, queryVector, topK, null);
    }

    @Override
    public List<RetrievedChunk> searchWithFilter(String collection, float[] queryVector, int topK, String filterExpr) {
        return doSearch(collection, queryVector, topK, filterExpr);
    }

    // ================== 内部方法 ==================

    private List<RetrievedChunk> doSearch(String collection, float[] queryVector, int topK, String filterExpr) {
        FloatVec queryData = new FloatVec(queryVector);
        SearchReq.SearchReqBuilder reqBuilder = SearchReq.builder()
                .collectionName(collection)
                .data(List.of(queryData))
                .outputFields(List.of("doc_id", "content", "metadata"));

        if (filterExpr != null && !filterExpr.isBlank()) {
            reqBuilder.filter(filterExpr);
        }

        SearchReq req = reqBuilder.topK(topK).build();
        SearchResp resp = milvusClient.search(req);

        List<RetrievedChunk> results = new ArrayList<>();
        List<List<SearchResp.SearchResult>> resultsData = resp.getSearchResults();
        if (resultsData != null && !resultsData.isEmpty()) {
            for (SearchResp.SearchResult hit : resultsData.get(0)) {
                Map<String, Object> entity = hit.getEntity();
                RetrievedChunk chunk = RetrievedChunk.builder()
                        .id(getEntityString(entity, "doc_id"))
                        .text(getEntityString(entity, "content"))
                        .score(hit.getScore())
                        .build();
                results.add(chunk);
            }
        }
        return results;
    }

    private List<JsonObject> buildChunkRows(List<VectorChunk> chunks, String docId, String collection, int dimension) {
        List<JsonObject> rows = new ArrayList<>(chunks.size());
        for (VectorChunk chunk : chunks) {
            float[] vector = resolveVector(chunk, dimension);
            String chunkPk = chunk.getChunkId() != null ? chunk.getChunkId() : SnowflakeIdUtil.nextIdStr();
            rows.add(buildRow(chunkPk, docId, chunk, collection, vector));
        }
        return rows;
    }

    private List<JsonObject> buildSingleChunkRow(VectorChunk chunk, String docId, String chunkPk,
                                                   String collection, int dimension) {
        float[] vector = resolveVector(chunk, dimension);
        return List.of(buildRow(chunkPk, docId, chunk, collection, vector));
    }

    private JsonObject buildRow(String chunkPk, String docId, VectorChunk chunk,
                                 String collection, float[] vector) {
        String content = truncateContent(chunk.getContent());
        JsonObject metadata = new JsonObject();
        metadata.addProperty("doc_id", docId);
        metadata.addProperty("kb_id", chunk.getKbId());
        if (chunk.getIndex() != null) {
            metadata.addProperty("chunk_index", chunk.getIndex());
        }
        if (chunk.getDocTitle() != null) {
            metadata.addProperty("doc_title", chunk.getDocTitle());
        }
        if (chunk.getSectionPath() != null) {
            metadata.addProperty("section_path", chunk.getSectionPath());
        }

        JsonObject row = new JsonObject();
        row.addProperty("doc_id", chunkPk);
        row.addProperty("content", content);
        row.add("metadata", metadata);
        row.add("embedding", toJsonArray(vector));
        return row;
    }

    private float[] resolveVector(VectorChunk chunk, int dimension) {
        float[] vector = chunk.getEmbedding();
        if (vector == null || vector.length == 0) {
            throw new ClientException("向量不能为空");
        }
        if (vector.length != dimension) {
            throw new ClientException("向量维度不匹配，期望 " + dimension + "，实际 " + vector.length);
        }
        return vector;
    }

    private int resolveDimension(String collection) {
        Integer override = getTenantDimension(collection);
        if (override != null) {
            return override;
        }
        return milvusProperties.getCollection().getDimension();
    }

    private Integer getTenantDimension(String collection) {
        if (milvusProperties.getTenants() == null) {
            return null;
        }
        return milvusProperties.getTenants().stream()
                .filter(t -> t.getCollectionName().equals(collection) && t.getDimension() != null)
                .findFirst()
                .map(MilvusProperties.TenantCollection::getDimension)
                .orElse(null);
    }

    private String truncateContent(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 65535 ? content.substring(0, 65535) : content;
    }

    private String getEntityString(Map<String, Object> entity, String field) {
        if (entity == null || !entity.containsKey(field)) {
            return null;
        }
        Object value = entity.get(field);
        return value != null ? value.toString() : null;
    }

    private JsonArray toJsonArray(float[] v) {
        JsonArray arr = new JsonArray(v.length);
        for (float x : v) {
            arr.add(x);
        }
        return arr;
    }
}
