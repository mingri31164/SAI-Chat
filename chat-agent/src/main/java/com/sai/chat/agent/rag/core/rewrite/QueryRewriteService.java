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

package com.sai.chat.agent.rag.core.rewrite;

import com.sai.chat.agent.framework.convention.rag.RewriteResult;

/**
 * 查询改写与拆分服务接口
 * <p>
 * 职责：
 * - 指代消解：将代词（这个、它）替换为具体实体
 * - 礼貌过滤：删除问候语、结束语
 * - 问题拆分：将复杂问题拆为多个独立子问题
 * - 语言规范化：修正口语化表达
 */
public interface QueryRewriteService {

    /**
     * 对用户问题进行改写与拆分
     *
     * @param question  原始用户问题
     * @param sessionId 会话 ID（用于获取对话历史）
     * @return 改写与拆分结果
     */
    RewriteResult rewrite(String question, String sessionId);

    /**
     * 仅做规则级改写（不使用 LLM，用于快速路径）
     * <p>
     * 应用场景：离线预处理 / 低延迟场景
     *
     * @param question 原始问题
     * @return 清理后的问题
     */
    String rewriteByRule(String question);
}
