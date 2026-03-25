/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file or compliance with
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.rag.constant.RAGConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 意图树缓存管理器
 * <p>
 * 负责从 Redis 读写意图树缓存，提供命中率优化。
 * 缓存命中时直接返回，避免每次从数据库加载。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentTreeCacheManager {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY = RAGConstant.INTENT_TREE_CACHE_KEY;
    private static final long EXPIRE_DAYS = RAGConstant.INTENT_TREE_CACHE_EXPIRE_DAYS;

    /**
     * 从 Redis 获取意图树缓存
     *
     * @return 意图树根节点列表，缓存不存在时返回 null
     */
    public List<IntentNode> getIntentTreeFromCache() {
        try {
            String cacheJson = stringRedisTemplate.opsForValue().get(CACHE_KEY);
            if (cacheJson == null || cacheJson.isBlank()) {
                log.debug("意图树缓存不存在，需要从数据库加载");
                return null;
            }
            List<IntentNode> roots = objectMapper.readValue(cacheJson, new TypeReference<>() {});
            log.debug("意图树缓存命中，根节点数: {}", roots.size());
            return roots;
        } catch (Exception e) {
            log.warn("从 Redis 读取意图树缓存失败", e);
            return null;
        }
    }

    /**
     * 将意图树保存到 Redis 缓存
     *
     * @param roots 意图树根节点列表
     */
    public void saveIntentTreeToCache(List<IntentNode> roots) {
        try {
            String cacheJson = objectMapper.writeValueAsString(roots);
            stringRedisTemplate.opsForValue().set(CACHE_KEY, cacheJson, EXPIRE_DAYS, TimeUnit.DAYS);
            log.info("意图树已保存到 Redis 缓存，根节点数: {}", roots.size());
        } catch (Exception e) {
            log.error("保存意图树到 Redis 缓存失败", e);
        }
    }

    /**
     * 清除意图树缓存
     */
    public void clearIntentTreeCache() {
        try {
            Boolean deleted = stringRedisTemplate.delete(CACHE_KEY);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("意图树缓存已清除，Key: {}", CACHE_KEY);
            }
        } catch (Exception e) {
            log.error("清除意图树缓存失败", e);
        }
    }

    /**
     * 从缓存的意图树中构建内存索引
     *
     * @param roots 意图树根节点列表
     * @return 意图树索引数据
     */
    public static IntentTreeData buildIndex(List<IntentNode> roots) {
        List<IntentNode> allNodes = flatten(roots);
        List<IntentNode> leafNodes = allNodes.stream()
                .filter(IntentNode::isLeaf)
                .collect(Collectors.toList());
        Map<String, IntentNode> id2Node = allNodes.stream()
                .collect(Collectors.toMap(IntentNode::getId, n -> n, (a, b) -> a));
        return new IntentTreeData(allNodes, leafNodes, id2Node);
    }

    /**
     * 递归将树扁平化为节点列表
     */
    private static List<IntentNode> flatten(List<IntentNode> roots) {
        List<IntentNode> result = new ArrayList<>();
        for (IntentNode root : roots) {
            flattenNode(root, result);
        }
        return result;
    }

    private static void flattenNode(IntentNode node, List<IntentNode> result) {
        result.add(node);
        if (node.getChildren() != null) {
            for (IntentNode child : node.getChildren()) {
                flattenNode(child, result);
            }
        }
    }

    /**
     * 意图树索引数据
     *
     * @param allNodes   所有节点（扁平化）
     * @param leafNodes  叶子节点列表
     * @param id2Node    ID → 节点映射
     */
    public record IntentTreeData(
            List<IntentNode> allNodes,
            List<IntentNode> leafNodes,
            Map<String, IntentNode> id2Node
    ) {}
}
