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

package com.sai.chat.agent.framework.convention.rag;

import com.sai.chat.agent.framework.convention.rag.enums.IntentKind;
import com.sai.chat.agent.framework.convention.rag.enums.IntentLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 意图节点数据模型
 * <p>
 * 表示意图树中的一个节点，包含节点 ID、名称、层级、类型等信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentNode {

    /**
     * 唯一标识，如："group-hr" / "biz-oa-intro" / "middleware-redis"
     */
    private String id;

    /**
     * 知识库 ID
     */
    private String kbId;

    /**
     * 展示名称，如"人事""OA系统""数据安全"
     */
    private String name;

    /**
     * 语义说明，用于向量化时的语义提示词
     */
    private String description;

    /**
     * 所属层级：DOMAIN / CATEGORY / TOPIC
     */
    private IntentLevel level;

    /**
     * 父节点 ID，根节点为 null
     */
    private String parentId;

    /**
     * 示例问题
     */
    @Builder.Default
    private List<String> examples = new ArrayList<>();

    /**
     * 子节点列表，没有子节点 = 叶子
     */
    @Builder.Default
    private List<IntentNode> children = new ArrayList<>();

    /**
     * 这类节点属于知识库还是系统交互
     */
    @Builder.Default
    private IntentKind kind = IntentKind.KB;

    /**
     * Milvus Collection 名称（仅对 kind=KB 有意义）
     */
    private String collectionName;

    /**
     * MCP 工具 ID（仅对 kind=MCP 有意义）
     */
    private String mcpToolId;

    /**
     * 节点级检索 TopK（可选）
     */
    private Integer topK;

    /**
     * 参数提取提示词模板（MCP 模式专属）
     */
    private String paramPromptTemplate;

    /**
     * SYSTEM 类型时使用的预设回答模板
     */
    private String promptTemplate;

    /**
     * 是否为"最终节点"（叶子节点）
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public boolean isKB() {
        return kind == null || kind == IntentKind.KB;
    }

    public boolean isMCP() {
        return kind == IntentKind.MCP;
    }

    public boolean isSystem() {
        return kind == IntentKind.SYSTEM;
    }
}
