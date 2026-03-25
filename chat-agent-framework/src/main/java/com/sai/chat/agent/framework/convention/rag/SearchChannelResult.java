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

import com.sai.chat.agent.framework.convention.RetrievedChunk;
import com.sai.chat.agent.framework.convention.rag.enums.SearchChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检索通道执行结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchChannelResult {

    /**
     * 通道类型
     */
    private SearchChannelType channelType;

    /**
     * 通道名称（用于日志和监控）
     */
    private String channelName;

    /**
     * 检索到的 Chunk 列表
     */
    private List<RetrievedChunk> chunks;

    /**
     * 通道置信度 (0-1)，表示该通道对结果的信心程度
     */
    private double confidence;

    /**
     * 检索耗时（毫秒）
     */
    private long latencyMs;

    /**
     * 扩展元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
