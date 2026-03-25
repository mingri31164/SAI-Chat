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

package com.sai.chat.agent.framework.convention.rag.enums;

/**
 * 意图类型枚举，标识一个意图节点属于哪一类处理方式
 */
public enum IntentKind {

    /**
     * 知识库类型，走 RAG 向量检索
     */
    KB(0),

    /**
     * 系统交互类型，直接返回预设回答（如欢迎语、自我介绍）
     */
    SYSTEM(1),

    /**
     * MCP 工具调用类型，执行实时数据查询等工具
     */
    MCP(2);

    private final int code;

    IntentKind(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
