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

package com.sai.chat.agent.rag.core.agent.reasoning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ReAct 推理结果
 * <p>
 * 封装 LLM 推理的完整输出，包括思考过程和动作选择。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReActReasoning {

    /**
     * 思考过程（Thought）
     */
    private String thought;

    /**
     * 解析出的动作
     */
    private ReActAction action;

    /**
     * 原始 LLM 输出（用于调试）
     */
    private String rawOutput;

    /**
     * 创建成功推理结果
     */
    public static ReActReasoning success(String thought, ReActAction action, String rawOutput) {
        return ReActReasoning.builder()
                .thought(thought)
                .action(action)
                .rawOutput(rawOutput)
                .build();
    }

    /**
     * 创建推理失败结果
     */
    public static ReActReasoning failure(String rawOutput) {
        return ReActReasoning.builder()
                .thought(null)
                .action(ReActAction.unknown())
                .rawOutput(rawOutput)
                .build();
    }

    /**
     * 判断推理是否成功
     */
    public boolean isSuccess() {
        return action != null && action.isValid();
    }
}
