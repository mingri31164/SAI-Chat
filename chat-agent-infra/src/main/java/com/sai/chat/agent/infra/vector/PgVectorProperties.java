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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * PgVector 向量数据库配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "pgvector")
public class PgVectorProperties {

    /**
     * pgvector JDBC 连接地址，例如 jdbc:postgresql://localhost:5432/sai_chat
     */
    private String jdbcUrl;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 默认表配置
     */
    private TableConfig table = new TableConfig();

    /**
     * 多租户表映射
     */
    private List<TenantTable> tenants = new ArrayList<>();

    @Data
    public static class TableConfig {
        /**
         * 默认表名
         */
        private String defaultName = "rag_vector_store";

        /**
         * 默认向量维度
         */
        private int dimension = 4096;

        /**
         * 距离度量类型：cosine_dist、ip、l2
         */
        private String metricType = "cosine_dist";

        /**
         * 索引类型：none、ivfflat、hnsw
         */
        private String indexType = "hnsw";

        /**
         * HNSW 构建参数 - 连接数（默认 16）
         */
        private int m = 16;

        /**
         * HNSW 构建参数 - 搜索邻居数（默认 64）
         */
        private int efConstruction = 64;

        /**
         * HNSW 搜索参数 ef（默认 64）
         */
        private int efSearch = 64;
    }

    @Data
    public static class TenantTable {
        /**
         * 知识库 ID / 租户 ID
         */
        private String tenantId;

        /**
         * 表名
         */
        private String tableName;

        /**
         * 向量维度（可覆盖默认值）
         */
        private Integer dimension;

        /**
         * 距离度量类型（可覆盖默认值）
         */
        private String metricType;

        /**
         * 是否启用
         */
        private boolean enabled = true;
    }
}
