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
import lombok.Getter;

/**
 * 歧义引导决策结果
 * <p>
 * 当检测到用户问题存在歧义时（如同名主题出现在多个系统中），
 * 返回一个需要向用户澄清的引导提示
 */
@Getter
@AllArgsConstructor
public class GuidanceDecision {

    /**
     * 决策动作类型
     */
    public enum Action {
        /**
         * 无需引导，继续正常流程
         */
        NONE,
        /**
         * 需要向用户展示澄清提示
         */
        PROMPT
    }

    private final Action action;
    private final String prompt;

    /**
     * 创建无需引导的决策
     */
    public static GuidanceDecision none() {
        return new GuidanceDecision(Action.NONE, null);
    }

    /**
     * 创建需要引导的决策
     */
    public static GuidanceDecision prompt(String prompt) {
        return new GuidanceDecision(Action.PROMPT, prompt);
    }

    public boolean isPrompt() {
        return action == Action.PROMPT;
    }

    public boolean isNone() {
        return action == Action.NONE;
    }
}
