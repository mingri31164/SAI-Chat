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

import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.NodeScore;

import java.util.List;

/**
 * 意图分类器接口
 * <p>
 * 负责将用户问题分类到对应的意图节点，返回置信度评分列表
 */
public interface IntentClassifier {

    /**
     * 对用户问题进行意图分类
     *
     * @param question 用户问题
     * @return 按置信度降序排列的意图节点评分列表
     */
    List<NodeScore> classifyTargets(String question);

    /**
     * 获取置信度最高的前 N 个意图（阈值过滤后）
     *
     * @param question 用户问题
     * @param topN    返回的最大数量
     * @param minScore 最低置信度阈值
     * @return 按置信度降序排列的意图节点评分列表（最多 topN 个）
     */
    default List<NodeScore> topKAboveThreshold(String question, int topN, double minScore) {
        List<NodeScore> all = classifyTargets(question);
        return all.stream()
                .filter(ns -> ns.getScore() >= minScore)
                .limit(topN)
                .toList();
    }
}
