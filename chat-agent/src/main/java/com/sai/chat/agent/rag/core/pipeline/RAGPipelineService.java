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

package com.sai.chat.agent.rag.core.pipeline;

import com.sai.chat.agent.framework.convention.rag.GuidanceDecision;

/**
 * RAG 流水线接口
 * <p>
 * 串联完整的 RAG 处理链路：
 * 1. 意图分类 → 2. 歧义检测 → 3. 查询改写 → 4. 多路检索 → 5. LLM 生成
 * <p>
 * 支持 SSE 流式输出，实时推送每一步的执行状态和最终答案
 */
public interface RAGPipelineService {

    /**
     * 执行完整的 RAG 流水线（同步版本）
     *
     * @param question  用户问题
     * @param sessionId 会话 ID
     * @return 最终回复文本
     */
    String chat(String question, String sessionId);

    /**
     * 检查是否需要歧义引导（同步版本）
     *
     * @param question  用户问题
     * @param sessionId 会话 ID
     * @return 引导决策
     */
    GuidanceDecision checkGuidance(String question, String sessionId);
}
