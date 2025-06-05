package com.mingri.core.Idempotent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class RetryWithBackoff<T> {

    private final int baseDelay; // 基础延迟（毫秒）
    private final int maxDelay; // 最大延迟（毫秒）
    private final int maxRetries; // 最大重试次数
    private final boolean useJitter; // 是否使用随机抖动

    public RetryWithBackoff(int baseDelay, int maxDelay, int maxRetries, boolean useJitter) {
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
        this.maxRetries = maxRetries;
        this.useJitter = useJitter;
    }

    /**
     * 执行带重试的获取操作，成功返回结果，否则返回 null。
     * @param action 获取操作（通常为查询缓存）
     * @return 成功获取的值，或 null
     */
    public T execute(Supplier<T> action) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            T result = action.get();
            if (result != null) {
                return result;
            }

            int delay = baseDelay * (1 << attempt);
            delay = Math.min(delay, maxDelay);

            if (useJitter) {
                int jitter = ThreadLocalRandom.current().nextInt(delay / 2);
                delay = delay / 2 + jitter; // 抖动：delay 介于 0.5x~1x 原始 delay
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }
}
