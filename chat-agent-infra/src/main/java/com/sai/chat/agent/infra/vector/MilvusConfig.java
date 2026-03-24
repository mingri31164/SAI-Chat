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

package com.sai.chat.agent.infra.vector;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库客户端配置
 * <p>
 * 通过 Spring 容器统一创建并管理 {@link MilvusClientV2} 实例，用于向量数据的增删改查、索引管理等操作
 * 支持通过配置文件设置连接地址与可选的访问令牌
 */
@Configuration
@RequiredArgsConstructor
public class MilvusConfig {

    private final MilvusProperties milvusProperties;

    /**
     * 构建 Milvus 客户端 Bean
     * <p>
     * 使用 {@code @Bean(destroyMethod = "close")} 保证在 Spring 容器关闭时，
     * 自动调用 {@link MilvusClientV2#close()} 释放连接资源
     */
    @Bean(destroyMethod = "close")
    public MilvusClientV2 milvusClient() {
        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                .uri(milvusProperties.getUri());

        String token = milvusProperties.getToken();
        if (token != null && !token.isBlank()) {
            builder.token(token);
        }

        return new MilvusClientV2(builder.build());
    }
}
