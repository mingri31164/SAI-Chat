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

import com.sai.chat.agent.framework.errorcode.BaseErrorCode;
import com.sai.chat.agent.framework.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PgVector 向量空间管理实现
 * <p>
 * 实现 {@link VectorStoreAdmin} 接口，负责表的 DDL 操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PgVectorStoreAdmin implements VectorStoreAdmin {

    private final DataSource pgVectorDataSource;
    private final PgVectorProperties pgVectorProperties;

    @Override
    public void createCollectionIfAbsent(String collection, int dimension, String metricType) {
        if (collectionExists(collection)) {
            log.debug("表已存在，跳过创建: {}", collection);
            return;
        }

        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id BIGSERIAL PRIMARY KEY,
                    doc_id VARCHAR(64) NOT NULL,
                    content TEXT,
                    embedding VECTOR(%d),
                    metadata JSONB,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """.formatted(collection, dimension);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(pgVectorDataSource);
            jdbcTemplate.execute(sql);
            log.info("PgVector 表创建成功, table={}, dimension={}, metricType={}",
                    collection, dimension, metricType);

            buildIndex(jdbcTemplate, collection, dimension, metricType,
                    pgVectorProperties.getTable().getIndexType());
        } catch (Exception e) {
            throw new ServiceException("PgVector 表创建失败: " + collection, e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public void dropCollectionIfPresent(String collection) {
        if (!collectionExists(collection)) {
            log.debug("表不存在，跳过删除: {}", collection);
            return;
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(pgVectorDataSource);
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + collection);
            log.info("PgVector 表删除成功, table={}", collection);
        } catch (Exception e) {
            throw new ServiceException("PgVector 表删除失败: " + collection, e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public boolean collectionExists(String collection) {
        try (Connection conn = pgVectorDataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String schema = conn.getSchema();
            try (ResultSet rs = metaData.getTables(null, schema, collection, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.warn("检查表是否存在时出错: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getCollectionDimension(String collection) {
        if (!collectionExists(collection)) {
            return -1;
        }

        String sql = """
                SELECT embedding::regclass::text
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = '%s'
                  AND column_name = 'embedding'
                """.formatted(collection);

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(pgVectorDataSource);
            String typeStr = jdbcTemplate.queryForObject(sql, String.class);
            if (typeStr == null) {
                return -1;
            }
            if (typeStr.contains("(")) {
                String dimStr = typeStr.substring(typeStr.indexOf("(") + 1, typeStr.indexOf(")"));
                return Integer.parseInt(dimStr.trim());
            }
        } catch (Exception e) {
            log.warn("获取表维度失败: {}", e.getMessage());
        }
        return pgVectorProperties.getTable().getDimension();
    }

    @Override
    public List<String> listCollections() {
        String sql = """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = current_schema()
                  AND table_type = 'BASE TABLE'
                  AND EXISTS (
                      SELECT 1 FROM information_schema.columns c
                      WHERE c.table_schema = current_schema()
                        AND c.table_name = tables.table_name
                        AND c.column_name = 'embedding'
                  )
                ORDER BY table_name
                """;

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(pgVectorDataSource);
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            log.warn("列出向量表时出错: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void buildIndex(JdbcTemplate jdbcTemplate, String table, int dimension,
                           String metricType, String indexType) {
        String indexName = table + "_embedding_idx";
        if (indexExists(jdbcTemplate, indexName)) {
            log.debug("索引已存在，跳过创建: {}", indexName);
            return;
        }

        String indexSql = switch (indexType.toLowerCase()) {
            case "ivfflat" -> String.format(
                    "CREATE INDEX IF NOT EXISTS %s ON %s USING ivfflat (embedding %s) WITH (lists = 100)",
                    indexName, table, metricType);
            case "hnsw" -> {
                int m = pgVectorProperties.getTable().getM();
                int efConstruction = pgVectorProperties.getTable().getEfConstruction();
                yield String.format(
                        "CREATE INDEX IF NOT EXISTS %s ON %s USING hnsw (embedding %s) WITH (m = %d, ef_construction = %d)",
                        indexName, table, metricType, m, efConstruction);
            }
            default -> {
                log.debug("索引类型为 none，跳过索引创建");
                yield null;
            }
        };

        if (indexSql != null) {
            jdbcTemplate.execute(indexSql);
            log.info("PgVector 索引创建成功, index={}, type={}", indexName, indexType);
        }
    }

    private boolean indexExists(JdbcTemplate jdbcTemplate, String indexName) {
        try (Connection conn = pgVectorDataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String schema = conn.getSchema();
            try (ResultSet rs = metaData.getIndexInfo(null, schema, null, true, false)) {
                while (rs.next()) {
                    if (indexName.equals(rs.getString("INDEX_NAME"))) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.warn("检查索引是否存在时出错: {}", e.getMessage());
        }
        return false;
    }
}
