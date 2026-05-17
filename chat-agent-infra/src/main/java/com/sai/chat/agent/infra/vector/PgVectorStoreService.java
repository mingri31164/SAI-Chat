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
import com.google.gson.JsonObject;
import com.mingri.toolkit.SnowflakeIdUtil;
import com.sai.chat.agent.framework.convention.RetrievedChunk;
import com.sai.chat.agent.framework.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PgVector 向量存储服务实现
 * <p>
 * 实现 {@link VectorStoreService} 接口，基于 JDBC + pgvector 扩展进行向量增删改查操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PgVectorStoreService implements VectorStoreService {

    private final JdbcTemplate pgVectorJdbcTemplate;
    private final PgVectorProperties pgVectorProperties;

    @Override
    public void indexDocumentChunks(String tableName, String docId, List<VectorChunk> chunks) {
        Assert.notEmpty(chunks, () -> new ClientException("文档分块不允许为空"));
        int dimension = resolveDimension(tableName);

        List<Object[]> batch = new ArrayList<>();
        for (VectorChunk chunk : chunks) {
            float[] vector = resolveVector(chunk, dimension);
            String chunkPk = chunk.getChunkId() != null ? chunk.getChunkId() : SnowflakeIdUtil.nextIdStr();
            String content = truncateContent(chunk.getContent());
            JsonObject metadata = buildMetadata(docId, chunk);
            String embedding = toPgVectorString(vector);
            batch.add(new Object[]{chunkPk, content, embedding, metadata.toString()});
        }

        String sql = """
                INSERT INTO %s (doc_id, content, embedding, metadata)
                VALUES (?, ?::vector, ?::jsonb)
                """.formatted(tableName);

        int[] results = pgVectorJdbcTemplate.batchUpdate(sql, batch);
        int totalInserted = 0;
        for (int r : results) {
            if (r > 0) {
                totalInserted++;
            } else if (r == 0) {
                totalInserted++;
            }
        }
        log.info("PgVector 写入向量索引成功, table={}, docId={}, rows={}", tableName, docId, totalInserted);
    }

    @Override
    public void updateChunk(String tableName, String docId, VectorChunk chunk) {
        Assert.notNull(chunk, () -> new ClientException("Chunk 对象不能为空"));
        int dimension = resolveDimension(tableName);
        float[] vector = resolveVector(chunk, dimension);
        String chunkPk = chunk.getChunkId() != null ? chunk.getChunkId() : SnowflakeIdUtil.nextIdStr();
        String content = truncateContent(chunk.getContent());
        JsonObject metadata = buildMetadata(docId, chunk);
        String embedding = toPgVectorString(vector);

        String sql = """
                UPDATE %s
                SET content = ?,
                    embedding = ?::vector,
                    metadata = ?::jsonb,
                    updated_at = CURRENT_TIMESTAMP
                WHERE doc_id = ?
                """.formatted(tableName);

        int updated = pgVectorJdbcTemplate.update(sql, content, embedding, metadata.toString(), chunkPk);
        if (updated == 0) {
            String insertSql = """
                    INSERT INTO %s (doc_id, content, embedding, metadata)
                    VALUES (?, ?::vector, ?::jsonb)
                    """.formatted(tableName);
            pgVectorJdbcTemplate.update(insertSql, chunkPk, content, embedding, metadata.toString());
            log.info("PgVector 更新向量索引（insert）成功, table={}, docId={}, chunkId={}", tableName, docId, chunkPk);
        } else {
            log.info("PgVector 更新向量索引（update）成功, table={}, docId={}, chunkId={}", tableName, docId, chunkPk);
        }
    }

    @Override
    public void deleteDocumentVectors(String tableName, String docId) {
        String sql = "DELETE FROM %s WHERE metadata->>'doc_id' = ?".formatted(tableName);
        int deleted = pgVectorJdbcTemplate.update(sql, docId);
        log.info("PgVector 删除文档向量成功, table={}, docId={}, deleteCnt={}", tableName, docId, deleted);
    }

    @Override
    public void deleteChunkById(String tableName, String chunkId) {
        String sql = "DELETE FROM %s WHERE doc_id = ?".formatted(tableName);
        int deleted = pgVectorJdbcTemplate.update(sql, chunkId);
        log.info("PgVector 删除 Chunk 成功, table={}, chunkId={}, deleteCnt={}", tableName, chunkId, deleted);
    }

    @Override
    public List<RetrievedChunk> search(String tableName, float[] queryVector, int topK) {
        return doSearch(tableName, queryVector, topK, null);
    }

    @Override
    public List<RetrievedChunk> searchWithFilter(String tableName, float[] queryVector, int topK, String filterExpr) {
        return doSearch(tableName, queryVector, topK, filterExpr);
    }

    // ================== 内部方法 ==================

    private List<RetrievedChunk> doSearch(String tableName, float[] queryVector, int topK, String filterExpr) {
        int dimension = resolveDimension(tableName);
        if (queryVector.length != dimension) {
            throw new ClientException("查询向量维度不匹配，期望 " + dimension + "，实际 " + queryVector.length);
        }

        String metricType = resolveMetricType(tableName);
        String embedding = toPgVectorString(queryVector);

        String sql;
        if (filterExpr != null && !filterExpr.isBlank()) {
            sql = """
                    SELECT doc_id, content, metadata,
                           embedding <=> ?::vector AS score
                    FROM %s
                    WHERE %s
                    ORDER BY embedding <=> ?::vector
                    LIMIT %d
                    """.formatted(tableName, filterExpr, topK);
            return pgVectorJdbcTemplate.query(sql, new RetrievedChunkRowMapper(), embedding, embedding);
        } else {
            sql = """
                    SELECT doc_id, content, metadata,
                           embedding <=> ?::vector AS score
                    FROM %s
                    ORDER BY embedding <=> ?::vector
                    LIMIT %d
                    """.formatted(tableName, topK);
            return pgVectorJdbcTemplate.query(sql, new RetrievedChunkRowMapper(), embedding);
        }
    }

    private int resolveDimension(String tableName) {
        Integer override = getTenantDimension(tableName);
        if (override != null) {
            return override;
        }
        return pgVectorProperties.getTable().getDimension();
    }

    private String resolveMetricType(String tableName) {
        if (pgVectorProperties.getTenants() != null) {
            return pgVectorProperties.getTenants().stream()
                    .filter(t -> t.getTableName().equals(tableName) && t.getMetricType() != null)
                    .findFirst()
                    .map(PgVectorProperties.TenantTable::getMetricType)
                    .orElse(pgVectorProperties.getTable().getMetricType());
        }
        return pgVectorProperties.getTable().getMetricType();
    }

    private Integer getTenantDimension(String tableName) {
        if (pgVectorProperties.getTenants() == null) {
            return null;
        }
        return pgVectorProperties.getTenants().stream()
                .filter(t -> t.getTableName().equals(tableName) && t.getDimension() != null)
                .findFirst()
                .map(PgVectorProperties.TenantTable::getDimension)
                .orElse(null);
    }

    private JsonObject buildMetadata(String docId, VectorChunk chunk) {
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
        return metadata;
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

    private String truncateContent(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 65535 ? content.substring(0, 65535) : content;
    }

    private String toPgVectorString(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(v[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private static class RetrievedChunkRowMapper implements RowMapper<RetrievedChunk> {
        @Override
        public RetrievedChunk mapRow(ResultSet rs, int rowNum) throws SQLException {
            RetrievedChunk chunk = RetrievedChunk.builder()
                    .id(rs.getString("doc_id"))
                    .text(rs.getString("content"))
                    .score((float) (1.0 - rs.getDouble("score")))
                    .build();
            return chunk;
        }
    }
}
