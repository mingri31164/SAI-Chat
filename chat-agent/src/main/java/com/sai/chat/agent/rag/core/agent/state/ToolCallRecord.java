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

package com.sai.chat.agent.rag.core.agent.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具调用轨迹记录
 * <p>
 * 记录 Agent 执行过程中每一次工具调用的完整信息，用于追踪和调试。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCallRecord {

    /**
     * 调用序号
     */
    private int stepIndex;

    /**
     * 工具 ID
     */
    private String toolId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 调用参数（脱敏处理）
     */
    private Map<String, Object> parameters;

    /**
     * 调用理由（为什么选择这个工具）
     */
    private String reasoning;

    /**
     * 执行结果
     */
    private ToolCallResult result;

    /**
     * 触发时间戳（毫秒）
     */
    private long timestamp;

    /**
     * 获取完整描述
     */
    public String getDescription() {
        return "Step " + stepIndex + ": " + toolName + 
               (result != null && !result.isSuccess() ? " [FAILED]" : "");
    }
}
