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

    /**
     * 获取启用的 Server 配置
     */
    public List<MCPClientConfig> getEnabledClients() {
        return clients.stream().filter(MCPClientConfig::isEnabled).toList();
    }

    public static class MCPClientConfig {

        private String name = "default";

        private String url = "http://localhost:9099/mcp";

        private int timeout = 30000;

        private boolean enabled = true;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
