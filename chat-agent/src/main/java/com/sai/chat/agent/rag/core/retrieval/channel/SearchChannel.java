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

package com.sai.chat.agent.rag.core.retrieval.channel;

import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.SearchChannelResult;
import com.sai.chat.agent.framework.convention.rag.SearchContext;

/**
 * 检索通道接口
 * <p>
 * 定义一种检索策略的抽象，每种通道对应一种检索方式。
 * 典型实现：
 * - {@link com.sai.chat.agent.rag.core.retrieval.channel.VectorGlobalSearchChannel}：向量全局检索
 * - {@link com.sai.chat.agent.rag.core.retrieval.channel.IntentDirectedSearchChannel}：意图定向检索
 */
public interface SearchChannel {

    /**
     * 执行检索
     *
     * @param ctx   检索上下文（含问题、意图等信息）
     * @param node  对应的意图节点（部分通道可能不需要）
     * @param topK  期望返回的结果数量
     * @return 检索结果
     */
    SearchChannelResult search(SearchContext ctx, IntentNode node, int topK);

    /**
     * 通道是否可用
     * <p>
     * 例如：当 Milvus 连接失败时，全局向量通道不可用
     *
     * @return true 表示通道可用
     */
    default boolean isAvailable() {
        return true;
    }
}
