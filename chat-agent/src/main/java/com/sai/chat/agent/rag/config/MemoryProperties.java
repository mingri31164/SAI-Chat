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

package com.sai.chat.agent.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 对话记忆配置属性
 * <p>
 * 通过 {@code rag.memory.*} 前缀在 application.yml 中配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.memory")
public class MemoryProperties {

    /**
     * 保留原文的最近轮数（user+assistant 视为一轮）
     */
    private int historyKeepTurns = 8;

    /**
     * 开始摘要的轮数阈值（需 > historyKeepTurns）
     */
    private int summaryStartTurns = 9;

    /**
     * 是否启用对话记忆压缩
     */
    private boolean summaryEnabled = false;

    /**
     * 缓存过期时间（分钟）
     */
    private int ttlMinutes = 60;

    /**
     * 摘要最大字数
     */
    private int summaryMaxChars = 200;

    /**
     * 会话标题最大长度
     */
    private int titleMaxLength = 30;
}
