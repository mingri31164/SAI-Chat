package com.mingri.core.Idempotent;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class IdempotentResultStore {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 存对象（json）
    public void saveResult(String key, Object result, long expireSeconds) {
        String json = JSONUtil.toJsonStr(result);
        redisTemplate.opsForValue().set(key, json, expireSeconds, TimeUnit.SECONDS);
    }

    // 反序列化取出对象
    public <T> T getResult(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        return JSONUtil.toBean(json, clazz);
    }

    // 通用反序列化（适用于AOP切面中动态类型）
    public Object deserializeResult(String json, Class<?> clazz) {
        if (json == null) return null;
        return JSONUtil.toBean(json, clazz);
    }
}