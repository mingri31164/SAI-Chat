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

package com.sai.chat.agent.rag.core.agent;

import com.sai.chat.agent.rag.config.AgentProperties;
import com.sai.chat.agent.rag.core.agent.executor.ReActAgentExecutor;
import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.agent.state.AgentMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Agent 服务门面
 * <p>
 * 提供统一的 Agent 执行入口，整合 RAG Pipeline 和 Agent 两种执行模式。
 * <p>
 * 执行流程：
 * <pre>
 * 1. 根据请求参数选择执行模式
 * 2. 构建 AgentRequest
 * 3. 调用对应的 AgentExecutor
 * 4. 返回 AgentResponse
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final ReActAgentExecutor reActAgentExecutor;
    private final AgentProperties agentProperties;

    /**
     * 执行 Agent
     *
     * @param request Agent 请求
     * @return Agent 响应
     */
    public AgentResponse execute(AgentRequest request) {
        if (!agentProperties.isEnabled()) {
            log.warn("Agent 模式未启用");
            return AgentResponse.failure("Agent 模式未启用", 0);
        }

        // 设置默认参数
        if (request.getMaxIterations() <= 0) {
            request.setMaxIterations(agentProperties.getMaxIterations());
        }
        if (request.getMaxTokens() <= 0) {
            request.setMaxTokens(agentProperties.getMaxTokens());
        }
        if (request.getMaxBudget() <= 0) {
            request.setMaxBudget(agentProperties.getMaxBudget());
        }
        if (request.getMode() == null) {
            request.setMode(AgentMode.valueOf(agentProperties.getDefaultMode()));
        }

        log.info("Agent 执行开始, mode={}, question={}", 
                request.getMode(), truncate(request.getQuestion(), 50));

        // 根据模式选择执行器
        return switch (request.getMode()) {
            case REACT -> reActAgentExecutor.execute(request);
            case PLAN_AND_EXECUTE, HYBRID -> {
                log.warn("PLAN_AND_EXECUTE 和 HYBRID 模式暂未实现，使用 REACT 模式");
                yield reActAgentExecutor.execute(request);
            }
        };
    }

    /**
     * 执行 Agent（流式）
     *
     * @param request  Agent 请求
     * @param callback 状态回调
     * @return Agent 响应
     */
    public AgentResponse executeStream(AgentRequest request, AgentCallback callback) {
        if (!agentProperties.isEnabled()) {
            log.warn("Agent 模式未启用");
            return AgentResponse.failure("Agent 模式未启用", 0);
        }

        // 设置默认参数
        if (request.getMaxIterations() <= 0) {
            request.setMaxIterations(agentProperties.getMaxIterations());
        }
        if (request.getMaxTokens() <= 0) {
            request.setMaxTokens(agentProperties.getMaxTokens());
        }
        if (request.getMaxBudget() <= 0) {
            request.setMaxBudget(agentProperties.getMaxBudget());
        }
        if (request.getMode() == null) {
            request.setMode(AgentMode.valueOf(agentProperties.getDefaultMode()));
        }

        log.info("Agent 流式执行开始, mode={}, question={}",
                request.getMode(), truncate(request.getQuestion(), 50));

        // 根据模式选择执行器
        return switch (request.getMode()) {
            case REACT -> reActAgentExecutor.executeStream(request, callback);
            case PLAN_AND_EXECUTE, HYBRID -> {
                log.warn("PLAN_AND_EXECUTE 和 HYBRID 模式暂未实现，使用 REACT 模式");
                yield reActAgentExecutor.executeStream(request, callback);
            }
        };
    }

    /**
     * 获取 Agent 执行器列表
     *
     * @return Agent 信息列表
     */
    public java.util.List<java.util.Map<String, String>> getAvailableAgents() {
        return java.util.List.of(
                java.util.Map.of(
                        "name", reActAgentExecutor.getName(),
                        "description", reActAgentExecutor.getDescription(),
                        "mode", "REACT"
                )
        );
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }
}
