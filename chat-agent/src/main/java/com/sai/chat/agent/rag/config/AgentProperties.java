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

package com.sai.chat.agent.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent 配置属性
 * <p>
 * 定义 Agent 执行过程中的各项配置参数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag.agent")
public class AgentProperties {

    /**
     * 是否启用 Agent 模式
     */
    private boolean enabled = true;

    /**
     * 默认 Agent 模式
     */
    private String defaultMode = "REACT";

    /**
     * 最大迭代次数
     */
    private int maxIterations = 10;

    /**
     * 最大 Token 数量限制
     */
    private int maxTokens = 8000;

    /**
     * 最大成本限制（元）
     */
    private double maxBudget = 1.0;

    /**
     * 默认温度
     */
    private double temperature = 0.7;

    /**
     * 是否启用深度思考模式
     */
    private boolean deepThinkingEnabled = false;

    /**
     * 是否启用反思机制
     */
    private boolean reflectionEnabled = true;

    /**
     * 是否启用流式输出
     */
    private boolean streamingEnabled = true;

    /**
     * 思考超时时间（毫秒）
     */
    private long thinkingTimeoutMs = 30000;

    /**
     * 工具调用超时时间（毫秒）
     */
    private long toolCallTimeoutMs = 60000;
}
