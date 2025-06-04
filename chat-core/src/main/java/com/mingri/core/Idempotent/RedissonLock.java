package com.mingri.core.Idempotent;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLock {

    @Autowired
    private RedissonClient redissonClient;

    public boolean tryLock(String key, long expireSeconds) {
        RLock lock = redissonClient.getLock(key);
        try {
            // waitTime=0 表示不等待，直接返回
            return lock.tryLock(0, expireSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}