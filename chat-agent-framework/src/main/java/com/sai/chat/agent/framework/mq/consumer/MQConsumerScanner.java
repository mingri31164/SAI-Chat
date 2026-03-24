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

package com.sai.chat.agent.framework.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MQ 消费者扫描器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MQConsumerScanner {

    private final ApplicationContext applicationContext;

    public List<MQConsumerDefinition> scan() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQConsumer.class);
        List<MQConsumerDefinition> definitions = new ArrayList<>();

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            if (!(bean instanceof MessageQueueConsumer<?> messageQueueConsumer)) {
                log.warn("Bean [{}] 标注了 @MQConsumer 但未实现 MessageQueueConsumer 接口，跳过", entry.getKey());
                continue;
            }

            MQConsumer annotation = applicationContext.findAnnotationOnBean(entry.getKey(), MQConsumer.class);
            if (annotation == null) {
                continue;
            }

            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Class<?> payloadType = ResolvableType.forClass(targetClass)
                    .as(MessageQueueConsumer.class)
                    .getGeneric(0)
                    .resolve(Object.class);

            @SuppressWarnings("unchecked")
            MessageQueueConsumer<Object> consumer = (MessageQueueConsumer<Object>) messageQueueConsumer;

            definitions.add(new MQConsumerDefinition(
                    entry.getKey(),
                    targetClass,
                    consumer,
                    annotation,
                    payloadType
            ));
        }

        return definitions;
    }
}
