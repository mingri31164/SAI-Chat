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

package com.sai.chat.agent.framework.config;

import com.sai.chat.agent.framework.mq.consumer.MQConsumerScanner;
import com.sai.chat.agent.framework.mq.consumer.RedisStreamConsumerBootstrap;
import com.sai.chat.agent.framework.mq.producer.MessageQueueProducer;
import com.sai.chat.agent.framework.mq.producer.RedisStreamProducer;
import com.sai.chat.agent.framework.mq.support.MessageWrapperCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis Stream 消息队列自动装配配置
 */
@Configuration
public class RedisStreamAutoConfiguration {

    @Bean
    public MessageWrapperCodec messageWrapperCodec() {
        return new MessageWrapperCodec();
    }

    @Bean
    public MessageQueueProducer messageQueueProducer(StringRedisTemplate stringRedisTemplate,
                                                     MessageWrapperCodec messageWrapperCodec) {
        return new RedisStreamProducer(stringRedisTemplate, messageWrapperCodec);
    }

    @Bean
    public RedisStreamConsumerBootstrap redisStreamConsumerBootstrap(RedisConnectionFactory redisConnectionFactory,
                                                                     StringRedisTemplate stringRedisTemplate,
                                                                     MQConsumerScanner mqConsumerScanner,
                                                                     MessageWrapperCodec messageWrapperCodec) {
        return new RedisStreamConsumerBootstrap(
                redisConnectionFactory,
                stringRedisTemplate,
                mqConsumerScanner,
                messageWrapperCodec
        );
    }
}
