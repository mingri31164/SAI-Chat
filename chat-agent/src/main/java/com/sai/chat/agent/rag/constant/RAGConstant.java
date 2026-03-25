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

package com.sai.chat.agent.rag.constant;

/**
 * RAG 模块核心常量定义
 */
public final class RAGConstant {

    private RAGConstant() {
    }

    // ================== 意图识别常量 ==================

    /**
     * 意图识别最低分数阈值
     * 低于这个分数就当成"聊偏了"，不参与 RAG 检索流程
     */
    public static final double INTENT_MIN_SCORE = 0.35;

    /**
     * 单次查询最多参与的意图数量上限
     * 防止拉取过多 Collection 导致性能问题
     */
    public static final int MAX_INTENT_COUNT = 3;

    // ================== 检索常量 ==================

    /**
     * 默认返回的 TopK
     */
    public static final int DEFAULT_TOP_K = 10;

    /**
     * 检索时的 TopK 扩展倍数
     */
    public static final int SEARCH_TOP_K_MULTIPLIER = 3;

    /**
     * 检索时的最小 TopK
     */
    public static final int MIN_SEARCH_TOP_K = 20;

    /**
     * Rerank 限制倍数
     */
    public static final int RERANK_LIMIT_MULTIPLIER = 2;

    // ================== 对话记忆常量 ==================

    /**
     * 保留原文的最近轮数（user+assistant 视为一轮）
     */
    public static final int DEFAULT_HISTORY_KEEP_TURNS = 4;

    /**
     * 开始摘要的轮数阈值（需 > historyKeepTurns）
     */
    public static final int DEFAULT_SUMMARY_START_TURNS = 5;

    /**
     * 摘要最大字数
     */
    public static final int DEFAULT_SUMMARY_MAX_CHARS = 200;

    // ================== 缓存常量 ==================

    /**
     * 意图树缓存 Key
     */
    public static final String INTENT_TREE_CACHE_KEY = "sai:intent:tree";

    /**
     * 意图树缓存过期时间（天）
     */
    public static final int INTENT_TREE_CACHE_EXPIRE_DAYS = 7;

    // ================== Prompt 模板路径 ==================

    /**
     * 意图分类 Prompt 模板路径
     */
    public static final String INTENT_CLASSIFIER_PROMPT_PATH = "prompt/intent-classifier.st";

    /**
     * 查询改写+拆分 Prompt 模板路径
     */
    public static final String QUERY_REWRITE_PROMPT_PATH = "prompt/query-rewrite.st";

    /**
     * 对话摘要 Prompt 模板路径
     */
    public static final String CONVERSATION_SUMMARY_PROMPT_PATH = "prompt/conversation-summary.st";

    /**
     * 摘要前缀（装饰摘要消息时使用）
     */
    public static final String SUMMARY_PREFIX = "对话摘要：";
}
