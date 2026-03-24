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
 * Milvus 向量数据库配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "milvus")
public class MilvusProperties {

    /**
     * Milvus 服务访问地址，例如 http://localhost:19530
     */
    private String uri;

    /**
     * 访问令牌（可选，未开启鉴权时可为空）
     */
    private String token;

    /**
     * 默认 Collection 配置
     */
    private CollectionConfig collection = new CollectionConfig();

    @Data
    public static class CollectionConfig {
        /**
         * 默认 Collection 名称
         */
        private String defaultName = "rag_default_store";

        /**
         * 默认向量维度
         */
        private int dimension = 4096;

        /**
         * 距离度量类型：COSINE、IP、L2
         */
        private String metricType = "COSINE";

        /**
         * 索引类型：FLAT、IVF_FLAT、HNSW 等
         */
        private String indexType = "FLAT";

        /**
         * 向量字段名
         */
        private String vectorField = "embedding";

        /**
         * 主键字段名
         */
        private String primaryField = "doc_id";

        /**
         * 内容字段名
         */
        private String contentField = "content";
    }

    /**
     * 多租户 Collection 映射
     * key: 知识库 ID / 租户 ID，value: Collection 配置
     */
    private List<TenantCollection> tenants = new ArrayList<>();

    @Data
    public static class TenantCollection {
        /**
         * 知识库 ID / 租户 ID
         */
        private String tenantId;

        /**
         * Collection 名称
         */
        private String collectionName;

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
