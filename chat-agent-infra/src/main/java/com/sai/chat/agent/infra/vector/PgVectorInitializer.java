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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PgVector 初始化器
 * <p>
 * 在 Spring 容器初始化时执行以下一次性操作：
 * 1. 启用 pgvector 扩展（CREATE EXTENSION IF NOT EXISTS vector）
 * 2. 创建默认向量存储表（如不存在）
 */
@Slf4j
@RequiredArgsConstructor
public class PgVectorInitializer {

    private final DataSource dataSource;
    private final PgVectorProperties pgVectorProperties;

    @PostConstruct
    public void init() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PgVectorProperties.TableConfig tableConfig = pgVectorProperties.getTable();
        String tableName = tableConfig.getDefaultName();

        enableExtension(jdbcTemplate);
        createTableIfAbsent(jdbcTemplate, tableName, tableConfig.getDimension());
        log.info("PgVector 初始化完成, table={}, dimension={}", tableName, tableConfig.getDimension());
    }

    private void enableExtension(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.debug("pgvector 扩展启用成功");
        } catch (Exception e) {
            log.warn("pgvector 扩展启用失败，请确认 PostgreSQL 已安装 pgvector 插件: {}", e.getMessage());
        }
    }

    private void createTableIfAbsent(JdbcTemplate jdbcTemplate, String tableName, int dimension) {
        if (tableExists(jdbcTemplate, tableName)) {
            log.debug("表已存在，跳过创建: {}", tableName);
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
                """.formatted(tableName, dimension);

        jdbcTemplate.execute(sql);
        log.info("PgVector 表创建成功, table={}, dimension={}", tableName, dimension);

        String indexType = pgVectorProperties.getTable().getIndexType();
        createIndexIfAbsent(jdbcTemplate, tableName, dimension, indexType);
    }

    private void createIndexIfAbsent(JdbcTemplate jdbcTemplate, String tableName,
                                     int dimension, String indexType) {
        String indexName = tableName + "_embedding_idx";
        if (indexExists(jdbcTemplate, indexName)) {
            log.debug("索引已存在，跳过创建: {}", indexName);
            return;
        }

        String metricType = pgVectorProperties.getTable().getMetricType();
        String vectorField = "embedding";

        String indexSql = switch (indexType.toLowerCase()) {
            case "ivfflat" -> String.format(
                    "CREATE INDEX IF NOT EXISTS %s ON %s USING ivfflat (%s %s) WITH (lists = 100)",
                    indexName, tableName, vectorField, metricType);
            case "hnsw" -> {
                int m = pgVectorProperties.getTable().getM();
                int efConstruction = pgVectorProperties.getTable().getEfConstruction();
                yield String.format(
                        "CREATE INDEX IF NOT EXISTS %s ON %s USING hnsw (%s %s) WITH (m = %d, ef_construction = %d)",
                        indexName, tableName, vectorField, metricType, m, efConstruction);
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

    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String schema = conn.getSchema();
            try (ResultSet rs = metaData.getTables(null, schema, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.warn("检查表是否存在时出错: {}", e.getMessage());
            return false;
        }
    }

    private boolean indexExists(JdbcTemplate jdbcTemplate, String indexName) {
        try (Connection conn = dataSource.getConnection()) {
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
