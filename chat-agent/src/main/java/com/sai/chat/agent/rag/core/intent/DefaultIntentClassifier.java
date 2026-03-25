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

package com.sai.chat.agent.rag.core.intent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.NodeScore;
import com.sai.chat.agent.rag.constant.RAGConstant;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.rag.core.prompt.PromptTemplateLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于 LLM 的树形意图分类器实现
 * <p>
 * 核心流程：
 * 1. 从 Redis 缓存或数据库加载意图树
 * 2. 将叶子节点注入 Prompt 模板
 * 3. 调用 LLM 进行分类打分
 * 4. 解析 JSON 响应并校验节点 ID
 * 5. 按置信度降序返回
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultIntentClassifier implements IntentClassifier, IntentNodeRegistry {

    private final LLMService llmService;
    private final IntentTreeCacheManager intentTreeCacheManager;
    private final PromptTemplateLoader promptTemplateLoader;

    /**
     * 最新加载的意图树索引
     */
    private volatile IntentTreeCacheManager.IntentTreeData cachedTreeData;

    @Override
    public List<NodeScore> classifyTargets(String question) {
        IntentTreeCacheManager.IntentTreeData data = loadIntentTreeData();

        String systemPrompt = buildPrompt(data.leafNodes());
        ChatRequest request = ChatRequest.builder()
                .messages(List.of(
                        ChatMessage.system(systemPrompt),
                        ChatMessage.user(question)
                ))
                .temperature(0.1D)
                .topP(0.3D)
                .thinking(false)
                .build();

        String raw = llmService.chat(request);

        return parseAndValidate(raw, data);
    }

    /**
     * 加载意图树数据（缓存优先）
     */
    private IntentTreeCacheManager.IntentTreeData loadIntentTreeData() {
        if (cachedTreeData != null) {
            return cachedTreeData;
        }

        List<IntentNode> roots = intentTreeCacheManager.getIntentTreeFromCache();

        if (roots == null || roots.isEmpty()) {
            roots = IntentTreeFactory.buildDefaultTree();
            intentTreeCacheManager.saveIntentTreeToCache(roots);
            log.info("意图树已从内存初始化并写入 Redis 缓存，根节点数: {}", roots.size());
        }

        IntentTreeCacheManager.IntentTreeData data = IntentTreeCacheManager.buildIndex(roots);
        this.cachedTreeData = data;
        return data;
    }

    /**
     * 构建意图分类 Prompt
     */
    private String buildPrompt(List<IntentNode> leafNodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是企业内部知识库意图分类助手，负责将用户问题路由到正确的知识分类节点。\n\n");
        sb.append("# 分类节点说明\n");
        sb.append("每个分类节点包含：\n");
        sb.append("- **id**：唯一标识\n");
        sb.append("- **path**：分类树路径（domain / category / topic）\n");
        sb.append("- **description**：知识范围说明\n");
        sb.append("- **examples**：典型问题示例\n\n");
        sb.append("# 选择规则\n");
        sb.append("- **默认**：只返回 1 个最核心的主意图分类\n");
        sb.append("- **例外**：仅当问题明确包含 2 个独立问题且需要不同知识库时，可返回 2 个（最多）\n\n");
        sb.append("# 评分标准\n");
        sb.append("- **> 0.8**：强匹配，关键实体/主题名称明确一致\n");
        sb.append("- **0.4-0.8**：中等相关，部分要素匹配\n");
        sb.append("- **< 0.4**：弱相关，建议返回空数组\n\n");
        sb.append("# 输出规范\n");
        sb.append("- 只输出 JSON 数组，无其他文字\n");
        sb.append("- 数组元素字段：id（字符串）、score（数值 0-1）\n\n");
        sb.append("# 示例\n");
        sb.append("[{\"id\": \"biz-oa-intro\", \"score\": 0.92}]\n\n");
        sb.append("# 分类列表\n");

        for (IntentNode node : leafNodes) {
            sb.append("- id=").append(node.getId()).append("\n");
            sb.append("  name=").append(node.getName()).append("\n");
            sb.append("  description=").append(node.getDescription()).append("\n");
            if (node.getExamples() != null && !node.getExamples().isEmpty()) {
                sb.append("  examples=").append(String.join(" / ", node.getExamples())).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 解析并校验 LLM 返回的 JSON
     */
    private List<NodeScore> parseAndValidate(String raw, IntentTreeCacheManager.IntentTreeData data) {
        if (raw == null || raw.isBlank()) {
            log.warn("LLM 返回为空");
            return List.of();
        }

        try {
            String cleaned = LLMResponseCleaner.stripMarkdownCodeFence(raw);

            JsonElement root = JsonParser.parseString(cleaned);

            JsonArray arr;
            if (root.isJsonArray()) {
                arr = root.getAsJsonArray();
            } else if (root.isJsonObject() && root.getAsJsonObject().has("results")) {
                arr = root.getAsJsonObject().getAsJsonArray("results");
            } else {
                log.warn("LLM 返回了非预期的 JSON 格式, 原始响应: {}", raw);
                return List.of();
            }

            List<NodeScore> scores = new ArrayList<>();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();

                if (!obj.has("id") || !obj.has("score")) continue;

                String id = obj.get("id").getAsString();
                double score = obj.get("score").getAsDouble();

                IntentNode node = data.id2Node().get(id);
                if (node == null) {
                    log.warn("LLM 返回了未知的意图节点 ID: {}, 已跳过", id);
                    continue;
                }

                scores.add(new NodeScore(node, score));
            }

            scores.sort(Comparator.comparingDouble(NodeScore::getScore).reversed());
            return scores;

        } catch (Exception e) {
            log.warn("解析 LLM 意图分类响应失败, 原始内容: {}", raw, e);
            return List.of();
        }
    }

    @Override
    public IntentNode getNodeById(String id) {
        IntentTreeCacheManager.IntentTreeData data = loadIntentTreeData();
        return data.id2Node().get(id);
    }
}
