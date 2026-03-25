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

package com.sai.chat.agent.rag.core.intent;

/**
 * LLM 响应清理工具
 * <p>
 * 负责清理 LLM 返回的 Markdown 代码块标记
 */
public final class LLMResponseCleaner {

    private LLMResponseCleaner() {
    }

    /**
     * 移除 Markdown 代码块标记
     *
     * @param raw 原始响应
     * @return 清理后的文本
     */
    public static String stripMarkdownCodeFence(String raw) {
        if (raw == null) {
            return null;
        }
        String result = raw.trim();

        // 移除 ```json ... ``` 包裹
        if (result.startsWith("```json")) {
            result = result.substring("```json".length());
        } else if (result.startsWith("```")) {
            result = result.substring("```".length());
        }

        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - "```".length());
        }

        return result.trim();
    }
}
