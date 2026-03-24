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

package com.sai.chat.agent.framework.mq.producer;

import com.sai.chat.agent.framework.mq.MessageWrapper;
import com.sai.chat.agent.framework.mq.support.MessageWrapperCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;

/**
 * 基于 Redis Stream 的消息生产者
 */
@Slf4j
@RequiredArgsConstructor
public class RedisStreamProducer implements MessageQueueProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final MessageWrapperCodec messageWrapperCodec;

    @Override
    public SendResult send(MessageWrapper<?> message) {
        try {
            String payload = messageWrapperCodec.serialize(message);
            StringRecord record = StreamRecords
                    .string(Collections.singletonMap("payload", payload))
                    .withStreamKey(message.getTopic());

            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
            if (recordId == null) {
                log.error("消息发送失败，Redis 未返回 recordId，topic: {}, messageId: {}", message.getTopic(), message.getId());
                return SendResult.fail(message.getId());
            }

            log.debug("消息发送成功，topic: {}, messageId: {}, recordId: {}", message.getTopic(), message.getId(), recordId.getValue());

            return SendResult.success(message.getId());
        } catch (Exception e) {
            log.error("消息发送失败，topic: {}, messageId: {}", message.getTopic(), message.getId(), e);
            return SendResult.fail(message.getId());
        }
    }
}
