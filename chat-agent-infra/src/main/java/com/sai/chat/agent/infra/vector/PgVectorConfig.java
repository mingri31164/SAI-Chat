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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * PgVector 向量数据库客户端配置
 * <p>
 * 通过 Spring 容器统一创建并管理 {@link DataSource} 实例，用于向量数据的增删改查、索引管理等操作。
 * 支持通过配置文件设置连接地址与访问凭证。
 * <p>
 * 初始化时自动：
 * - 启用 pgvector 扩展
 * - 创建默认向量存储表（如不存在）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PgVectorConfig {

    private final PgVectorProperties pgVectorProperties;

    @Bean
    public DataSource pgVectorDataSource() {
        PgVectorProperties.TableConfig tableConfig = pgVectorProperties.getTable();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(pgVectorProperties.getJdbcUrl());
        hikariConfig.setUsername(pgVectorProperties.getUsername());
        hikariConfig.setPassword(pgVectorProperties.getPassword());
        hikariConfig.setPoolName("PgVectorHikariPool");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        log.info("PgVector 数据源配置完成, jdbcUrl={}", pgVectorProperties.getJdbcUrl());
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public JdbcTemplate pgVectorJdbcTemplate(DataSource pgVectorDataSource) {
        return new JdbcTemplate(pgVectorDataSource);
    }

    @Bean
    public PgVectorInitializer pgVectorInitializer(DataSource pgVectorDataSource,
                                                   PgVectorProperties pgVectorProperties) {
        return new PgVectorInitializer(pgVectorDataSource, pgVectorProperties);
    }
}
