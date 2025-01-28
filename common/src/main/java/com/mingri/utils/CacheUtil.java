package com.mingri.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheUtil {
    //记录用户最后一次查询记录的用户Id
    private final Cache<String, String> userReadMsgCache;
    //用户登录信息<用户名，token>
    private final Cache<String, String> userSessionCache;

    public CacheUtil() {
        this.userReadMsgCache = Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build();
        this.userSessionCache = Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build();
    }

    public void putUserSessionCache(String username, String token) {
        userSessionCache.put(username, token);
    }

    public String getUserSessionCache(String username) {
        return userSessionCache.getIfPresent(username); //返回null表示缓存中没有该值
    }

    public void putUserReadCache(String userId, String targetId) {
        userReadMsgCache.put(userId, targetId);
    }

    public String getUserReadCache(String userId) {
        return userReadMsgCache.getIfPresent(userId);
    }
    public void clearUserSessionCache(String username) {
        userSessionCache.invalidate(username);
    }

    public void clearUserReadCache(String userId) {
        userReadMsgCache.invalidate(userId);
    }

    public void clearAllCaches() {
        userSessionCache.invalidateAll();
        userReadMsgCache.invalidateAll();
    }
}
