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
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Milvus 向量空间管理实现
 * <p>
 * 实现 {@link VectorStoreAdmin} 接口，负责 Collection 的 DDL 操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MilvusVectorStoreAdmin implements VectorStoreAdmin {

    private final MilvusClientV2 milvusClient;
    private final MilvusProperties milvusProperties;

    @Override
    public void createCollectionIfAbsent(String collection, int dimension, String metricType) {
        if (collectionExists(collection)) {
            log.debug("Collection 已存在，跳过创建: {}", collection);
            return;
        }

        CreateCollectionReq req = CreateCollectionReq.builder()
                .collectionName(collection)
                .dimension(dimension)
                .metricType(metricType)
                .description("Auto-created by chat-agent")
                .build();

        try {
            milvusClient.createCollection(req);
            log.info("Milvus Collection 创建成功, collection={}, dimension={}, metricType={}",
                    collection, dimension, metricType);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                log.debug("Collection 已存在（并发创建）: {}", collection);
                return;
            }
            throw new ServiceException("Milvus Collection 创建失败: " + collection, e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public void dropCollectionIfPresent(String collection) {
        if (!collectionExists(collection)) {
            log.debug("Collection 不存在，跳过删除: {}", collection);
            return;
        }

        DropCollectionReq req = DropCollectionReq.builder()
                .collectionName(collection)
                .build();

        milvusClient.dropCollection(req);
        log.info("Milvus Collection 删除成功, collection={}", collection);
    }

    @Override
    public boolean collectionExists(String collection) {
        HasCollectionReq req = HasCollectionReq.builder()
                .collectionName(collection)
                .build();
        Boolean exists = milvusClient.hasCollection(req);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public int getCollectionDimension(String collection) {
        if (!collectionExists(collection)) {
            return -1;
        }

        DescribeCollectionReq req = DescribeCollectionReq.builder()
                .collectionName(collection)
                .build();
        DescribeCollectionResp resp = milvusClient.describeCollection(req);

        CreateCollectionReq.CollectionSchema schema = resp.getCollectionSchema();
        if (schema == null || schema.getFieldSchemaList() == null) {
            return -1;
        }
        for (CreateCollectionReq.FieldSchema field : schema.getFieldSchemaList()) {
            Integer dim = field.getDimension();
            if (dim != null) {
                return dim;
            }
        }
        return -1;
    }

    @Override
    public List<String> listCollections() {
        ListCollectionsResp resp = milvusClient.listCollections();
        List<String> names = resp.getCollectionNames();
        return names != null ? names : new ArrayList<>();
    }
}
