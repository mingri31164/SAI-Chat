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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 远程 MCP 工具执行器
 * <p>
 * 将远程 MCP Server 上的工具调用包装为本地执行器接口。
 * 对 RAG 链路中的工具调用方透明，无需关心工具实际运行在远程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteMCPToolExecutor {

    private final HttpMCPClient mcpClient;

    /**
     * 调用远程 MCP 工具
     *
     * @param serverName MCP Server 名称
     * @param toolId     工具 ID
     * @param parameters 工具参数
     * @return 工具执行结果文本
     */
    public String execute(String serverName, String toolId, Map<String, Object> parameters) {
        log.info("MCP 工具调用, server={}, toolId={}, params={}", serverName, toolId, parameters);
        long start = System.currentTimeMillis();

        try {
            String result = mcpClient.callTool(serverName, toolId, parameters);
            log.info("MCP 工具执行成功, server={}, toolId={}, latency={}ms, resultLen={}",
                    serverName, toolId, System.currentTimeMillis() - start, result.length());
            return result;

        } catch (Exception e) {
            log.error("MCP 工具执行失败, server={}, toolId={}", serverName, toolId, e);
            return "MCP 工具 [" + toolId + "] 调用失败: " + e.getMessage();
        }
    }
}
