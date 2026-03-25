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

/**
 * RAG SSE 流式事件类型枚举
 * <p>
 * 定义服务端向客户端推送的所有事件类型
 */
public final class SSEventType {

    private SSEventType() {
    }

    /**
     * 意图分类结果事件
     */
    public static final String INTENT = "intent";

    /**
     * 查询改写结果事件
     */
    public static final String REWRITE = "rewrite";

    /**
     * 检索结果事件
     */
    public static final String RETRIEVAL = "retrieval";

    /**
     * LLM 思考过程事件
     */
    public static final String THINKING = "thinking";

    /**
     * LLM 回复流式内容事件
     */
    public static final String ANSWER = "answer";

    /**
     * 最终回复完成事件
     */
    public static final String DONE = "done";

    /**
     * 错误事件
     */
    public static final String ERROR = "error";

    /**
     * 歧义引导事件
     */
    public static final String GUIDANCE = "guidance";
}
