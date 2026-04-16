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

package com.sai.chat.agent.rag.core.observe.metrics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 指标配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "sai.agent.observe")
public class ObserveProperties {

    /**
     * 是否启用追踪
     */
    private boolean traceEnabled = true;

    /**
     * 是否启用指标采集
     */
    private boolean metricsEnabled = true;

    /**
     * 追踪最大存储数量
     */
    private int maxTraceStoreSize = 1000;

    /**
     * 指标上报间隔(秒)
     */
    private int metricsReportIntervalSeconds = 60;

    /**
     * 是否在控制台输出指标
     */
    private boolean consoleMetricsEnabled = true;

    /**
     * 指标保留时间(小时)
     */
    private int metricsRetentionHours = 24;

    /**
     * 延迟百分位配置
     */
    private Percentiles percentiles = new Percentiles();

    @Data
    public static class Percentiles {
        /**
         * P50延迟阈值(ms)
         */
        private double p50Threshold = 1000;

        /**
         * P95延迟阈值(ms)
         */
        private double p95Threshold = 3000;

        /**
         * P99延迟阈值(ms)
         */
        private double p99Threshold = 5000;
    }
}
