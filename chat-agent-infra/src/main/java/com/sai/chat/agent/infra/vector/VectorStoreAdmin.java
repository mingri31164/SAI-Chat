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

import java.util.List;

/**
 * 向量空间管理接口（Collection 级别的 DDL 操作）
 * <p>
 * 定义 Collection 的创建、删除、存在性检查等元数据管理操作
 */
public interface VectorStoreAdmin {

    /**
     * 创建 Collection（如已存在则跳过）
     *
     * @param collection Collection 名称
     * @param dimension 向量维度
     * @param metricType 距离度量类型：COSINE、IP、L2
     */
    void createCollectionIfAbsent(String collection, int dimension, String metricType);

    /**
     * 删除 Collection（如不存在则跳过）
     *
     * @param collection Collection 名称
     */
    void dropCollectionIfPresent(String collection);

    /**
     * 检查 Collection 是否存在
     *
     * @param collection Collection 名称
     * @return 是否存在
     */
    boolean collectionExists(String collection);

    /**
     * 获取 Collection 的向量维度
     *
     * @param collection Collection 名称
     * @return 向量维度，不存在时返回 -1
     */
    int getCollectionDimension(String collection);

    /**
     * 列出所有 Collection
     *
     * @return Collection 名称列表
     */
    List<String> listCollections();
}
