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

package com.sai.chat.agent.rag.core.agent.request;

import com.sai.chat.agent.rag.core.agent.state.AgentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Agent 执行请求
 * <p>
 * 定义启动 Agent 执行所需的完整参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentRequest {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 用户问题
     */
    private String question;

    /**
     * Agent 执行模式
     */
    @Builder.Default
    private AgentMode mode = AgentMode.REACT;

    /**
     * 最大迭代次数
     */
    @Builder.Default
    private int maxIterations = 10;

    /**
     * 最大 Token 数量限制
     */
    @Builder.Default
    private int maxTokens = 8000;

    /**
     * 最大成本限制（元）
     */
    @Builder.Default
    private double maxBudget = 1.0;

    /**
     * 是否启用深度思考模式
     */
    @Builder.Default
    private boolean deepThinking = false;

    /**
     * 允许使用的工具 ID 列表
     * 如果为空或 null，表示可以使用所有可用工具
     */
    private List<String> allowedTools;

    /**
     * 禁止使用的工具 ID 列表
     */
    private List<String> excludedTools;

    /**
     * 扩展参数
     */
    private Map<String, Object> parameters;
}
