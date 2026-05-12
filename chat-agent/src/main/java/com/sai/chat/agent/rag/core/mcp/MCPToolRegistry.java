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

package com.sai.chat.agent.rag.core.mcp;

import com.sai.chat.agent.rag.config.RAGProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 工具注册与发现服务
 * <p>
 * 职责：
 * - 启动时从配置的 MCP Server 动态发现可用工具
 * - 按 toolId 查询工具定义
 * - 管理工具与 IntentNode 的映射关系
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MCPToolRegistry {

    private final HttpMCPClient mcpClient;
    private final RAGProperties ragProperties;

    /**
     * toolId → MCP 工具定义
     */
    private final Map<String, MCPToolDefinition> toolById = new ConcurrentHashMap<>();

    /**
     * toolId → 所属 Server 名称
     */
    private final Map<String, String> serverByToolId = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        RAGProperties.MCPConfig mcpConfig = ragProperties.getMcp();
        if (!mcpConfig.isEnabled()) {
            log.info("MCP 功能已禁用");
            return;
        }

        String serverName = mcpConfig.getServerName();
        String serverUrl = mcpConfig.getServerUrl();

        try {
            // 通过 HttpMCPClient 初始化（内部会从 MCPClientProperties 获取 URL）
            // 简化为直接发现工具
            List<MCPToolDefinition> tools = mcpClient.discoverAllTools();

            for (MCPToolDefinition tool : tools) {
                toolById.put(tool.getToolId(), tool);
                serverByToolId.put(tool.getToolId(), serverName);
            }

            log.info("MCP Server 初始化成功, 发现 {} 个工具", tools.size());

        } catch (Exception e) {
            log.warn("MCP Server 初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 按 toolId 获取工具定义
     */
    public Optional<MCPToolDefinition> getTool(String toolId) {
        return Optional.ofNullable(toolById.get(toolId));
    }

    /**
     * 获取工具所属的 Server 名称
     */
    public Optional<String> getServerName(String toolId) {
        return Optional.ofNullable(serverByToolId.get(toolId));
    }

    /**
     * 获取所有已注册的工具
     */
    public List<MCPToolDefinition> getAllTools() {
        return List.copyOf(toolById.values());
    }

    /**
     * 获取所有工具 ID
     */
    public Set<String> getAllToolIds() {
        return Set.copyOf(toolById.keySet());
    }
}
