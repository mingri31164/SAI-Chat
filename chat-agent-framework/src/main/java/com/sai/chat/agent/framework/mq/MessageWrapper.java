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

package com.sai.chat.agent.framework.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * 消息队列统一消息体封装
 *
 * @param <T> 业务载荷类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageWrapper<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一 ID，默认自动生成
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /**
     * 目标 topic
     */
    private String topic;

    /**
     * 业务 key，可用于幂等判断
     */
    private String keys;

    /**
     * 业务载荷
     */
    private T body;

    /**
     * 发送时间戳，默认当前时间
     */
    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
