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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 客户端配置属性
 * <p>
 * 声明式配置各 MCP Server 实例：
 * <pre>
 * mcp:
 *   clients:
 *     - name: default
 *       url: http://localhost:9099/mcp
 *       timeout: 30000
 *       enabled: true
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "sai.mcp")
public class MCPClientProperties {

    /**
     * 是否启用 MCP 工具调用
     */
    private boolean enabled = true;

    /**
     * MCP Server 实例列表
     */
    private List<MCPClientConfig> clients = new ArrayList<>();

    @Data
    public static class MCPClientConfig {
        /**
         * Server 实例名称（用于区分多个 MCP Server）
         */
        private String name = "default";

        /**
         * MCP Server HTTP 端点
         */
        private String url = "http://localhost:9099/mcp";

        /**
         * HTTP 请求超时（毫秒）
         */
        private int timeout = 30000;

        /**
         * 是否启用此 Server
         */
        private boolean enabled = true;
    }
}
