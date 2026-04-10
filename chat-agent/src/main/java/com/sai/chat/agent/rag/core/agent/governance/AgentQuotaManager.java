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

package com.sai.chat.agent.rag.core.agent.governance;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent 配额管理
 * <p>
 * 负责跟踪和限制用户/会话级别的资源消耗：
 * <ul>
 *   <li>请求次数限制（每分钟/每小时/每天）</li>
 *   <li>Token 消耗限制</li>
 *   <li>成本限制</li>
 *   <li>并发执行限制</li>
 * </ul>
 */
@Slf4j
@Component
public class AgentQuotaManager {

    /**
     * 默认每分钟最大请求数
     */
    private static final int DEFAULT_MINUTE_LIMIT = 10;

    /**
     * 默认每小时最大请求数
     */
    private static final int DEFAULT_HOUR_LIMIT = 100;

    /**
     * 默认每天最大请求数
     */
    private static final int DEFAULT_DAY_LIMIT = 500;

    /**
     * 默认最大 Token 消耗
     */
    private static final int DEFAULT_MAX_TOKENS = 100_000;

    /**
     * 默认最大成本（元）
     */
    private static final double DEFAULT_MAX_COST = 10.0;

    /**
     * 默认最大并发
     */
    private static final int DEFAULT_MAX_CONCURRENCY = 3;

    /**
     * 用户配额配置（userId -> QuotaConfig）
     */
    private final Map<String, QuotaConfig> userQuotaConfigs = new ConcurrentHashMap<>();

    /**
     * 用户配额使用情况（userId -> QuotaUsage）
     */
    private final Map<String, QuotaUsage> userQuotaUsage = new ConcurrentHashMap<>();

    /**
     * 会话配额使用情况（sessionId -> QuotaUsage）
     */
    private final Map<String, QuotaUsage> sessionQuotaUsage = new ConcurrentHashMap<>();

    /**
     * 用户当前并发数
     */
    private final Map<String, AtomicInteger> userConcurrency = new ConcurrentHashMap<>();

    /**
     * 配额检查结果
     */
    public record QuotaCheckResult(
            boolean allowed,
            String reason,
            QuotaUsage currentUsage,
            QuotaUsage limits
    ) {
        public static QuotaCheckResult allowed(QuotaUsage current, QuotaUsage limits) {
            return new QuotaCheckResult(true, null, current, limits);
        }

        public static QuotaCheckResult rejected(String reason, QuotaUsage current, QuotaUsage limits) {
            return new QuotaCheckResult(false, reason, current, limits);
        }
    }

    /**
     * 配额配置
     */
    @Data
    public static class QuotaConfig {
        private int minuteLimit = DEFAULT_MINUTE_LIMIT;
        private int hourLimit = DEFAULT_HOUR_LIMIT;
        private int dayLimit = DEFAULT_DAY_LIMIT;
        private int maxTokens = DEFAULT_MAX_TOKENS;
        private double maxCost = DEFAULT_MAX_COST;
        private int maxConcurrency = DEFAULT_MAX_CONCURRENCY;
        private boolean enabled = true;

        public static QuotaConfig defaultConfig() {
            return new QuotaConfig();
        }
    }

    /**
     * 配额使用情况
     */
    @Data
    public static class QuotaUsage {
        private final String key;
        private final Instant windowStart;
        private final Instant hourWindowStart;
        private final Instant dayWindowStart;

        private AtomicInteger minuteCount = new AtomicInteger(0);
        private AtomicInteger hourCount = new AtomicInteger(0);
        private AtomicInteger dayCount = new AtomicInteger(0);
        private AtomicInteger totalTokens = new AtomicInteger(0);
        private AtomicLong totalCostMs = new AtomicLong(0); // 毫秒精度存储，放大1000
        private AtomicInteger currentConcurrency = new AtomicInteger(0);

        public QuotaUsage(String key) {
            this.key = key;
            this.windowStart = Instant.now();
            this.hourWindowStart = Instant.now();
            this.dayWindowStart = Instant.now();
        }

        public double getCost() {
            return totalCostMs.get() / 1000.0;
        }

        public void addCost(double cost) {
            totalCostMs.addAndGet((long) (cost * 1000));
        }

        public void incrementMinute() {
            minuteCount.incrementAndGet();
            hourCount.incrementAndGet();
            dayCount.incrementAndGet();
        }

        public void decrementConcurrency() {
            currentConcurrency.decrementAndGet();
        }

        public int getMinuteCountValue() { return minuteCount.get(); }
        public int getHourCountValue() { return hourCount.get(); }
        public int getDayCountValue() { return dayCount.get(); }
        public int getTotalTokensValue() { return totalTokens.get(); }
        public int getCurrentConcurrencyValue() { return currentConcurrency.get(); }

        public void resetMinuteWindow(Instant now) {
            minuteCount.set(0);
        }

        public void resetHourWindow(Instant now) {
            hourCount.set(0);
        }

        public void resetDayWindow(Instant now) {
            dayCount.set(0);
        }
    }

    /**
     * 检查配额
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param cost      预估成本
     * @return 配额检查结果
     */
    public QuotaCheckResult checkQuota(String userId, String sessionId, double cost) {
        QuotaConfig config = userQuotaConfigs.getOrDefault(userId, QuotaConfig.defaultConfig());
        if (!config.isEnabled()) {
            return QuotaCheckResult.allowed(createDefaultUsage("default"), configToUsage(config, "default"));
        }

        Instant now = Instant.now();

        // 获取或创建用户配额
        QuotaUsage userUsage = userQuotaUsage.computeIfAbsent(userId, k -> new QuotaUsage(k));

        // 获取或创建会话配额
        QuotaUsage sessionUsage = sessionQuotaUsage.computeIfAbsent(sessionId, k -> new QuotaUsage(k));

        // 清理过期窗口
        cleanupWindows(userUsage, now);
        cleanupWindows(sessionUsage, now);

        // 检查并发
        int concurrency = userConcurrency.computeIfAbsent(userId, k -> new AtomicInteger(0)).get();
        if (concurrency >= config.getMaxConcurrency()) {
            return QuotaCheckResult.rejected(
                    "并发数超限: 当前 " + concurrency + ", 最大 " + config.getMaxConcurrency(),
                    userUsage, configToUsage(config, userId));
        }

        // 检查分钟限额
        if (userUsage.getMinuteCountValue() >= config.getMinuteLimit()) {
            return QuotaCheckResult.rejected(
                    "分钟请求数超限: 当前 " + userUsage.getMinuteCountValue() + ", 最大 " + config.getMinuteLimit(),
                    userUsage, configToUsage(config, userId));
        }

        // 检查小时限额
        if (userUsage.getHourCountValue() >= config.getHourLimit()) {
            return QuotaCheckResult.rejected(
                    "小时请求数超限: 当前 " + userUsage.getHourCountValue() + ", 最大 " + config.getHourLimit(),
                    userUsage, configToUsage(config, userId));
        }

        // 检查天限额
        if (userUsage.getDayCountValue() >= config.getDayLimit()) {
            return QuotaCheckResult.rejected(
                    "天请求数超限: 当前 " + userUsage.getDayCountValue() + ", 最大 " + config.getDayLimit(),
                    userUsage, configToUsage(config, userId));
        }

        // 检查 Token 限额
        if (userUsage.getTotalTokensValue() >= config.getMaxTokens()) {
            return QuotaCheckResult.rejected(
                    "Token 消耗超限: 当前 " + userUsage.getTotalTokensValue() + ", 最大 " + config.getMaxTokens(),
                    userUsage, configToUsage(config, userId));
        }

        // 检查成本限额
        if (userUsage.getCost() + cost > config.getMaxCost()) {
            return QuotaCheckResult.rejected(
                    "成本超限: 当前 " + userUsage.getCost() + ", 预估 " + cost + ", 最大 " + config.getMaxCost(),
                    userUsage, configToUsage(config, userId));
        }

        return QuotaCheckResult.allowed(userUsage, configToUsage(config, userId));
    }

    /**
     * 记录配额使用
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param tokens    消耗 Token
     * @param cost      消耗成本
     */
    public void recordUsage(String userId, String sessionId, int tokens, double cost) {
        QuotaUsage userUsage = userQuotaUsage.get(userId);
        QuotaUsage sessionUsage = sessionQuotaUsage.get(sessionId);

        if (userUsage != null) {
            userUsage.incrementMinute();
            userUsage.getTotalTokens().addAndGet(tokens);
            userUsage.addCost(cost);
        }

        if (sessionUsage != null) {
            sessionUsage.incrementMinute();
        }

        log.debug("记录配额使用, userId={}, sessionId={}, tokens={}, cost={}", userId, sessionId, tokens, cost);
    }

    /**
     * 申请执行位（增加并发计数）
     */
    public boolean acquireConcurrency(String userId) {
        AtomicInteger counter = userConcurrency.computeIfAbsent(userId, k -> new AtomicInteger(0));
        QuotaConfig config = userQuotaConfigs.getOrDefault(userId, QuotaConfig.defaultConfig());
        int current = counter.get();
        if (current < config.getMaxConcurrency()) {
            counter.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * 释放执行位（减少并发计数）
     */
    public void releaseConcurrency(String userId) {
        AtomicInteger counter = userConcurrency.get(userId);
        if (counter != null) {
            counter.decrementAndGet();
        }
        QuotaUsage userUsage = userQuotaUsage.get(userId);
        if (userUsage != null) {
            userUsage.decrementConcurrency();
        }
    }

    /**
     * 设置用户配额配置
     */
    public void setUserQuota(String userId, QuotaConfig config) {
        userQuotaConfigs.put(userId, config);
        log.info("设置用户配额, userId={}, config={}", userId, config);
    }

    /**
     * 获取用户配额使用情况
     */
    public QuotaUsage getUserUsage(String userId) {
        return userQuotaUsage.get(userId);
    }

    /**
     * 清理过期窗口
     */
    private void cleanupWindows(QuotaUsage usage, Instant now) {
        // 分钟窗口（1分钟）
        if (ChronoUnit.MINUTES.between(usage.getWindowStart(), now) >= 1) {
            usage.resetMinuteWindow(now);
        }
        // 小时窗口（1小时）
        if (ChronoUnit.HOURS.between(usage.getHourWindowStart(), now) >= 1) {
            usage.resetHourWindow(now);
        }
        // 天窗口（24小时）
        if (ChronoUnit.DAYS.between(usage.getDayWindowStart(), now) >= 1) {
            usage.resetDayWindow(now);
        }
    }

    private QuotaUsage createDefaultUsage(String key) {
        return new QuotaUsage(key);
    }

    private QuotaUsage configToUsage(QuotaConfig config, String key) {
        QuotaUsage usage = new QuotaUsage(key);
        return usage;
    }
}
