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
 * 意图节点所属的层级
 */
public enum IntentLevel {

    /**
     * 顶层（领域）：集团信息化 / 业务系统 / 中间件环境信息
     */
    DOMAIN(0),

    /**
     * 第二层（类别）：人事 / 行政 / OA系统 / Redis ...
     */
    CATEGORY(1),

    /**
     * 第三层（主题）：更具体的 Topic，如 系统介绍 / 数据安全 / 架构设计
     */
    TOPIC(2);

    private final int code;

    IntentLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
