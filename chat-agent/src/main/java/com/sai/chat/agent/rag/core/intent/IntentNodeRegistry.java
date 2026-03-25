/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You under the Apache License, Version 2.0
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

package com.sai.chat.agent.rag.core.intent;

import com.sai.chat.agent.framework.convention.rag.IntentNode;

/**
 * 意图节点注册表
 * <p>
 * 提供意图节点的查找能力，供其他组件（如引导服务）使用
 */
public interface IntentNodeRegistry {

    /**
     * 根据节点 ID 获取意图节点
     *
     * @param id 节点 ID
     * @return 意图节点，不存在返回 null
     */
    IntentNode getNodeById(String id);
}
