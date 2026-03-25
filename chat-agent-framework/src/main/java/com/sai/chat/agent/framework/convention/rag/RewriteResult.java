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

import java.util.List;

/**
 * 问题重写与拆分结果
 * <p>
 * 包含 LLM 改写后的问题以及拆分的子问题列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewriteResult {

    /**
     * 经过 LLM 优化后的问题
     * 删除了礼貌用语、无关描述，可用于整体相关性判断
     */
    private String rewrittenQuestion;

    /**
     * 拆分的子问题列表，每个子问题独立进行意图识别和检索
     */
    private List<String> subQuestions;
}
