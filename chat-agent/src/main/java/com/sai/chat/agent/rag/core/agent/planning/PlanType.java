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

package com.sai.chat.agent.rag.core.agent.planning;

/**
 * 计划类型枚举
 */
public enum PlanType {
    /**
     * 单步直接执行（RAG 或工具调用）
     */
    SINGLE_STEP,

    /**
     * ReAct 模式（边推理边执行）
     */
    REACT,

    /**
     * 先规划后执行（两阶段）
     */
    PLAN_THEN_EXECUTE,

    /**
     * 并行探索（多个方向同时探索）
     */
    PARALLEL_EXPLORE
}
