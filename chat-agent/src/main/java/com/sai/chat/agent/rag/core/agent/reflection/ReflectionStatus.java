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

package com.sai.chat.agent.rag.core.agent.reflection;

/**
 * 反思结果枚举
 * <p>
 * 描述对工具执行结果的评估状态。
 */
public enum ReflectionStatus {

    /**
     * 成功 - 工具执行成功，结果满足任务需求
     */
    SUCCESS,

    /**
     * 部分成功 - 工具执行成功，但结果不完整
     */
    PARTIAL,

    /**
     * 失败 - 工具执行失败
     */
    FAILURE,

    /**
     * 不确定 - 无法判断是否成功
     */
    UNCERTAIN
}
