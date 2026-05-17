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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量 Chunk 实体
 * <p>
 * 表示文档被切分后的单个片段，包含原始文本、预计算的向量以及元数据信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorChunk {

    /**
     * Chunk 唯一标识（对应 pgvector 主键 doc_id）
     */
    private String chunkId;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 预计算的 float 向量
     */
    private float[] embedding;

    /**
     * Chunk 在文档中的顺序索引（从 0 开始）
     */
    private Integer index;

    /**
     * 所属文档 ID
     */
    private String docId;

    /**
     * 所属知识库 / 租户 ID
     */
    private String kbId;

    /**
     * 文档标题（可选，用于展示）
     */
    private String docTitle;

    /**
     * 章节路径（如 "第3章 / 3.2 节"）
     */
    private String sectionPath;

    /**
     * 自定义元数据（KV 结构）
     */
    private String metadata;
}
