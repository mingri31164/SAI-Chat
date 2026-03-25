/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may not use this file or copy the Software, and
 * to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 */

package com.sai.chat.agent.rag.core.mcp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP 工具定义（客户端用）
 * <p>
 * 描述远程 MCP Server 上的一个可用工具及其参数规范。
 * 由 HttpMCPClient 通过 /tools/list 动态发现。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPToolDefinition {

    /**
     * 工具唯一标识，对应 tools/call 的 name 参数
     */
    private String toolId;

    /**
     * 工具显示名称
     */
    private String name;

    /**
     * 工具功能描述
     */
    private String description;

    /**
     * 参数字段定义
     * key: 参数名
     * value: 参数属性
     */
    private Map<String, ParameterDef> parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterDef {
        /**
         * 参数类型：string / number / boolean / object / array
         */
        private String type;

        /**
         * 参数描述
         */
        private String description;

        /**
         * 是否必填
         */
        private boolean required;

        /**
         * 枚举值列表（可选）
         */
        private List<String> enumValues;
    }
}
