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

package com.sai.chat.agent.framework.convention.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节点评分结果
 * <p>
 * 封装意图节点及其对应的 LLM 置信度分数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeScore {

    /**
     * 意图节点（包含 KB collection 或 MCP tool 信息）
     */
    private IntentNode node;

    /**
     * LLM 打出的置信度分数 [0, 1]
     */
    private double score;
}
