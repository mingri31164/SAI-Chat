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

import com.sai.chat.agent.framework.convention.rag.GuidanceDecision;
import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.NodeScore;
import com.sai.chat.agent.rag.config.RAGProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 意图引导服务
 * <p>
 * 负责在用户问题存在歧义时生成澄清提示，引导用户明确自己的意图
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentGuidanceService {

    private final RAGProperties ragProperties;
    private final IntentNodeRegistry intentNodeRegistry;

    /**
     * 根据意图识别结果进行歧义检测和引导
     * <p>
     * 歧义触发条件：最高分与次高分的比值 >= 配置阈值
     * 例如用户问题可能同时匹配两个不同的知识库节点时
     *
     * @param question     用户问题
     * @param nodeScores  意图分类结果（已按置信度降序）
     * @return 引导决策：是否需要引导提示
     */
    public GuidanceDecision makeGuidanceDecision(String question, List<NodeScore> nodeScores) {
        if (!ragProperties.getGuidance().isEnabled()) {
            return GuidanceDecision.none();
        }

        if (nodeScores == null || nodeScores.size() < 2) {
            return GuidanceDecision.none();
        }

        NodeScore top = nodeScores.get(0);
        NodeScore second = nodeScores.get(1);

        if (top.getScore() <= 0) {
            return GuidanceDecision.none();
        }

        double ratio = second.getScore() / top.getScore();

        if (ratio >= ragProperties.getGuidance().getAmbiguityScoreRatio()) {
            log.info("检测到歧义：用户问题=[{}], top={}, second={}, ratio={}",
                    question, top.getScore(), second.getScore(), ratio);
            return buildAmbiguityPrompt(question, nodeScores);
        }

        return GuidanceDecision.none();
    }

    private GuidanceDecision buildAmbiguityPrompt(String question, List<NodeScore> nodeScores) {
        StringBuilder sb = new StringBuilder();
        sb.append("您的问题「").append(question).append("」可能涉及多个主题：\n\n");

        int count = Math.min(ragProperties.getGuidance().getMaxOptions(), nodeScores.size());
        for (int i = 0; i < count; i++) {
            NodeScore ns = nodeScores.get(i);
            IntentNode node = ns.getNode();
            String path = buildPathString(node);
            sb.append(i + 1).append(". [").append(node.getName()).append("] - ").append(path).append("\n");
        }

        sb.append("\n请问您想了解哪个方面？");
        return GuidanceDecision.prompt(sb.toString());
    }

    private String buildPathString(IntentNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getName());
        if (node.getDescription() != null && !node.getDescription().isBlank()) {
            sb.append("：").append(node.getDescription());
        }
        return sb.toString();
    }
}
