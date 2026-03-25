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

import com.google.gson.*;
import com.sai.chat.agent.rag.core.mcp.MCPClientProperties.MCPClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * HTTP MCP 客户端
 * <p>
 * 通过 HTTP POST JSON-RPC 2.0 调用远程 MCP Server。
 * 支持：
 * - 工具列表发现（tools/list）
 * - 工具调用（tools/call）
 * - 初始化握手（initialize）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpMCPClient {

    private final MCPClientProperties properties;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final Map<String, OkHttpClient> clientsByName = new HashMap<>();
    private final Map<String, List<MCPToolDefinition>> toolCacheByName = new HashMap<>();

    /**
     * 获取指定名称的 HTTP Client（带超时）
     */
    private OkHttpClient getClient(MCPClientConfig config) {
        return clientsByName.computeIfAbsent(config.getName(), n ->
                new OkHttpClient.Builder()
                        .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                        .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                        .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                        .build()
        );
    }

    /**
     * 初始化 MCP Server 连接
     */
    public void initialize(String serverName) {
        MCPClientConfig config = findConfig(serverName);
        if (config == null) {
            throw new IllegalArgumentException("MCP Server not found: " + serverName);
        }

        JsonObject params = new JsonObject();
        JsonObject capabilities = new JsonObject();
        capabilities.addProperty("tools", true);
        params.add("capabilities", capabilities);

        JsonObject clientInfo = new JsonObject();
        clientInfo.addProperty("name", "chat-agent");
        clientInfo.addProperty("version", "1.0");
        params.add("clientInfo", clientInfo);

        JsonRpcCall(config, "initialize", params, null);
        log.info("MCP Server 初始化成功: {}", serverName);
    }

    /**
     * 获取所有已注册工具定义
     */
    public List<MCPToolDefinition> listTools(String serverName) {
        MCPClientConfig config = findConfig(serverName);
        if (config == null) {
            return List.of();
        }

        // 优先使用缓存
        if (toolCacheByName.containsKey(serverName)) {
            return toolCacheByName.get(serverName);
        }

        JsonObject params = new JsonObject();
        JsonObject response = JsonRpcCall(config, "tools/list", params, 1);

        List<MCPToolDefinition> tools = parseToolsList(response);
        toolCacheByName.put(serverName, tools);
        log.info("MCP Server {} 工具列表已缓存, 共 {} 个", serverName, tools.size());
        return tools;
    }

    /**
     * 调用指定工具
     *
     * @param serverName  Server 名称
     * @param toolId      工具 ID
     * @param arguments   工具参数
     * @return 工具执行结果文本
     */
    public String callTool(String serverName, String toolId, Map<String, Object> arguments) {
        MCPClientConfig config = findConfig(serverName);
        if (config == null) {
            throw new IllegalArgumentException("MCP Server not found: " + serverName);
        }

        JsonObject params = new JsonObject();
        params.addProperty("name", toolId);
        if (arguments != null && !arguments.isEmpty()) {
            params.add("arguments", new Gson().toJsonTree(arguments).getAsJsonObject());
        }

        JsonObject response = JsonRpcCall(config, "tools/call", params, 1);

        // 解析结果中的 content.text
        return parseToolResult(response);
    }

    /**
     * 发现所有 Server 上的所有工具
     */
    public List<MCPToolDefinition> discoverAllTools() {
        List<MCPToolDefinition> all = new ArrayList<>();
        for (MCPClientConfig config : properties.getClients()) {
            if (!config.isEnabled()) continue;
            try {
                all.addAll(listTools(config.getName()));
            } catch (Exception e) {
                log.warn("发现 MCP Server {} 工具失败", config.getName(), e);
            }
        }
        return all;
    }

    // ================== 内部方法 ==================

    private MCPClientConfig findConfig(String serverName) {
        return properties.getClients().stream()
                .filter(c -> c.getName().equals(serverName) && c.isEnabled())
                .findFirst()
                .orElse(null);
    }

    private JsonObject JsonRpcCall(MCPClientConfig config, String method, JsonObject params, Integer id) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("jsonrpc", "2.0");
        requestBody.addProperty("method", method);
        if (params != null) {
            requestBody.add("params", params);
        }
        if (id != null) {
            requestBody.addProperty("id", id);
        }

        Request request = new Request.Builder()
                .url(config.getUrl())
                .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = getClient(config).newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("MCP HTTP 请求失败: " + response);
            }

            String bodyStr = response.body() != null ? response.body().string() : "{}";
            JsonObject jsonResponse = JsonParser.parseString(bodyStr).getAsJsonObject();

            if (jsonResponse.has("error")) {
                JsonObject error = jsonResponse.getAsJsonObject("error");
                throw new RuntimeException("MCP 工具调用失败: " + error.get("message"));
            }

            return jsonResponse;

        } catch (IOException e) {
            log.error("MCP Server 调用失败, server={}, method={}", config.getName(), method, e);
            throw new RuntimeException("MCP Server 调用失败: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<MCPToolDefinition> parseToolsList(JsonObject response) {
        List<MCPToolDefinition> tools = new ArrayList<>();

        if (!response.has("result")) return tools;

        JsonObject result = response.getAsJsonObject("result");
        if (!result.has("tools")) return tools;

        JsonArray toolsArr = result.getAsJsonArray("tools");
        if (toolsArr == null) return tools;

        Gson gson = new Gson();
        for (JsonElement el : toolsArr) {
            try {
                MCPToolDefinition tool = parseToolSchema(el.getAsJsonObject(), gson);
                tools.add(tool);
            } catch (Exception e) {
                log.warn("解析工具定义失败: {}", el, e);
            }
        }

        return tools;
    }

    private MCPToolDefinition parseToolSchema(JsonObject schema, Gson gson) {
        String name = schema.has("name") ? schema.get("name").getAsString() : "";
        String description = schema.has("description") ? schema.get("description").getAsString() : "";

        Map<String, MCPToolDefinition.ParameterDef> params = new LinkedHashMap<>();

        if (schema.has("inputSchema") || schema.has("input_schema")) {
            JsonObject inputSchema = schema.has("inputSchema")
                    ? schema.getAsJsonObject("inputSchema")
                    : schema.getAsJsonObject("input_schema");

            if (inputSchema != null && inputSchema.has("properties")) {
                JsonObject props = inputSchema.getAsJsonObject("properties");
                for (String key : props.keySet()) {
                    JsonObject prop = props.getAsJsonObject(key);
                    MCPToolDefinition.ParameterDef pd = MCPToolDefinition.ParameterDef.builder()
                            .type(prop.has("type") ? prop.get("type").getAsString() : "string")
                            .description(prop.has("description") ? prop.get("description").getAsString() : "")
                            .required(false)
                            .build();

                    if (prop.has("enum")) {
                        List<String> enums = new ArrayList<>();
                        for (JsonElement e : prop.getAsJsonArray("enum")) {
                            enums.add(e.getAsString());
                        }
                        pd.setEnumValues(enums);
                    }

                    params.put(key, pd);
                }
            }
        }

        return MCPToolDefinition.builder()
                .toolId(name)
                .name(name)
                .description(description)
                .parameters(params)
                .build();
    }

    private String parseToolResult(JsonObject response) {
        if (!response.has("result")) {
            return "MCP 工具调用无结果";
        }

        JsonObject result = response.getAsJsonObject("result");
        if (!result.has("content")) {
            return "MCP 工具调用无 content 结果";
        }

        JsonArray content = result.getAsJsonArray("content");
        StringBuilder sb = new StringBuilder();
        for (JsonElement el : content) {
            JsonObject item = el.getAsJsonObject();
            if ("text".equals(item.get("type").getAsString()) && item.has("text")) {
                sb.append(item.get("text").getAsString());
            }
        }

        return sb.toString();
    }
}
