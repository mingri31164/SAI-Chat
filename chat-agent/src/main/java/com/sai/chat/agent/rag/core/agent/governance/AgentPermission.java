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

package com.sai.chat.agent.rag.core.agent.governance;

/**
 * Agent 权限等级
 * <p>
 * 用于控制不同用户角色可使用的 Agent 能力范围。
 */
public enum AgentPermission {

    /**
     * 只读模式（仅允许 RAG 查询，禁止工具调用）
     */
    READ_ONLY(1),

    /**
     * 标准模式（允许 RAG + 受限工具调用）
     */
    STANDARD(2),

    /**
     * 增强模式（允许 RAG + 全部工具调用）
     */
    ENHANCED(3),

    /**
     * 管理模式（无限制，含敏感工具）
     */
    ADMIN(10);

    private final int level;

    AgentPermission(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    /**
     * 判断是否满足所需权限
     */
    public boolean satisfies(AgentPermission required) {
        return this.level >= required.level;
    }

    /**
     * 判断是否允许使用指定工具
     */
    public boolean canUseTool(String toolId, boolean isSensitive) {
        if (isSensitive) {
            return this == ADMIN;
        }
        return this.level >= STANDARD.level;
    }
}
