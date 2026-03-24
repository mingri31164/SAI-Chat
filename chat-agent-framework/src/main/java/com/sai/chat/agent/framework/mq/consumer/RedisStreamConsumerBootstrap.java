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

import com.sai.chat.agent.framework.mq.MessageWrapper;
import com.sai.chat.agent.framework.mq.support.MessageWrapperCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis Stream 消费者启动器
 *
 * <p>
 * 实现 {@link SmartLifecycle}，在 Spring 容器启动时扫描所有 {@link MQConsumer} 注解的 Bean，
 * 为每个消费者创建 {@link StreamMessageListenerContainer}，绑定 consumer group 并开始监听
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class RedisStreamConsumerBootstrap implements SmartLifecycle {

    private final RedisConnectionFactory redisConnectionFactory;
    private final StringRedisTemplate stringRedisTemplate;
    private final MQConsumerScanner mqConsumerScanner;
    private final MessageWrapperCodec messageWrapperCodec;

    private final List<StreamMessageListenerContainer<String, MapRecord<String, String, String>>> containers = new ArrayList<>();
    private final List<ExecutorService> executors = new ArrayList<>();

    private volatile boolean running = false;

    @Override
    public void start() {
        List<MQConsumerDefinition> consumerDefinitions = mqConsumerScanner.scan();
        if (consumerDefinitions.isEmpty()) {
            log.info("未发现 @MQConsumer 注解的消费者 Bean，跳过 Redis Stream 消费者注册");
            return;
        }

        String consumerName = getConsumerName();

        for (MQConsumerDefinition definition : consumerDefinitions) {
            String topic = definition.getTopic();
            String consumerGroup = definition.getConsumerGroup();

            createConsumerGroupIfAbsent(topic, consumerGroup);

            ExecutorService executor = buildConsumerExecutor(topic);
            executors.add(executor);

            StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                    StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                            .batchSize(10)
                            .executor(executor)
                            .pollTimeout(Duration.ofSeconds(3))
                            .build();

            StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                    StreamMessageListenerContainer.create(redisConnectionFactory, options);

            StreamListener<String, MapRecord<String, String, String>> listener =
                    new RedisStreamMessageListener(
                            definition.consumer(),
                            definition.payloadType(),
                            stringRedisTemplate,
                            messageWrapperCodec
                    );

            StreamMessageListenerContainer.StreamReadRequest<String> readRequest =
                    StreamMessageListenerContainer.StreamReadRequest
                            .builder(StreamOffset.create(topic, ReadOffset.lastConsumed()))
                            .cancelOnError(throwable -> false)
                            .consumer(Consumer.from(consumerGroup, consumerName))
                            .autoAcknowledge(true)
                            .build();

            container.register(readRequest, listener);
            container.start();
            containers.add(container);

            log.info("Redis Stream 消费者注册成功，bean: {}, topic: {}, consumerGroup: {}, consumerName: {}",
                    definition.beanName(), topic, consumerGroup, consumerName);
        }

        running = true;
    }

    @Override
    public void stop() {
        doStop();
    }

    @Override
    public void stop(@NonNull Runnable callback) {
        try {
            doStop();
        } finally {
            callback.run();
        }
    }

    private void doStop() {
        log.info("开始停止 Redis Stream 消费者...");

        // 1. 先停止容器，避免继续拉取消息
        for (StreamMessageListenerContainer<String, MapRecord<String, String, String>> container : containers) {
            try {
                container.stop();
            } catch (Exception e) {
                log.warn("停止 StreamMessageListenerContainer 异常", e);
            }
        }

        // 2. 再优雅关闭线程池
        for (ExecutorService executor : executors) {
            shutdownGracefully(executor);
        }

        containers.clear();
        executors.clear();
        running = false;

        log.info("Redis Stream 消费者已全部停止");
    }

    private void shutdownGracefully(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination((long) 30, TimeUnit.SECONDS)) {
                log.warn("线程池未在规定时间内优雅退出，准备强制关闭");
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("线程池强制关闭失败");
                }
            }
        } catch (InterruptedException e) {
            log.warn("等待线程池关闭被中断，准备强制关闭");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 让它在应用关闭时尽量早停止，避免依赖资源已先被销毁
        return Integer.MAX_VALUE;
    }

    private void createConsumerGroupIfAbsent(String topic, String consumerGroup) {
        try {
            stringRedisTemplate.opsForStream().createGroup(topic, consumerGroup);
            log.info("创建 consumer group 成功，topic: {}, group: {}", topic, consumerGroup);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("Consumer group 已存在，topic: {}, group: {}", topic, consumerGroup);
            } else {
                log.warn("创建 consumer group 异常，topic: {}, group: {}", topic, consumerGroup, e);
            }
        }
    }

    private ExecutorService buildConsumerExecutor(String topic) {
        AtomicInteger index = new AtomicInteger();
        return new ThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("stream_consumer_" + topic + "_" + index.incrementAndGet());
                    thread.setDaemon(false);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private String getConsumerName() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + ProcessHandle.current().pid();
        } catch (Exception e) {
            return "consumer-" + System.currentTimeMillis();
        }
    }

    @Slf4j
    private record RedisStreamMessageListener(
            MessageQueueConsumer<Object> consumer,
            Class<?> payloadType,
            StringRedisTemplate stringRedisTemplate,
            MessageWrapperCodec messageWrapperCodec
    ) implements StreamListener<String, MapRecord<String, String, String>> {

        @Override
        public void onMessage(MapRecord<String, String, String> message) {
            String streamKey = message.getStream();
            RecordId recordId = message.getId();

            try {
                assert streamKey != null;
                String payload = message.getValue().get("payload");
                if (payload == null) {
                    log.warn("收到空 payload 消息，streamKey: {}, recordId: {}", streamKey, recordId);
                    stringRedisTemplate.opsForStream().delete(streamKey, recordId);
                    return;
                }

                MessageWrapper<Object> wrapper = messageWrapperCodec.deserialize(payload, payloadType);
                consumer.consume(wrapper);

                stringRedisTemplate.opsForStream().delete(streamKey, recordId);
                log.debug("消息消费并删除成功，streamKey: {}, recordId: {}", streamKey, recordId);
            } catch (Exception e) {
                log.error("消息消费失败，streamKey: {}, recordId: {}", streamKey, recordId, e);
            }
        }
    }
}
