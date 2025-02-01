package com.mingri.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mingri.annotation.UrlLimit;
import com.mingri.constant.JwtClaimsConstant;
import com.mingri.constant.MessageConstant;
import com.mingri.constant.UrlLimitStats;
import com.mingri.constant.type.LimitKeyType;
import com.mingri.exception.BaseException;
import com.mingri.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
@Slf4j
public class UrlLimitAspect {
    private final Cache<String, AtomicInteger> requestCountCache;
    private final Cache<String, UrlLimitStats> statsCache;

    public UrlLimitAspect() {
        // 创建请求计数缓存
        this.requestCountCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
        // 创建统计信息缓存
        this.statsCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Pointcut("@annotation(com.mingri.annotation.UrlLimit)")
    public void rateLimitPointcut() {
    }

    @Around("rateLimitPointcut() && @annotation(urlLimit)")
    public Object around(ProceedingJoinPoint joinPoint, UrlLimit urlLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String key = "";
        // 获取key的类型
        if (urlLimit.keyType() == LimitKeyType.ID) {
            Map<String, Object> userinfo = (Map<String, Object>) request.getAttribute("userinfo");
            key = userinfo.get(JwtClaimsConstant.USER_ID).toString();
        } else {
            key = IpUtil.getIpAddr(request);
        }
        String path = request.getRequestURI();
        key = key + ":" + path;
        // 检查是否被封禁
        UrlLimitStats stats = statsCache.get(key, k -> new UrlLimitStats());
        if (stats.isBlocked()) {
            throw new BaseException(MessageConstant.ACCESS_FREQUENCY_LIMIT_EXCEEDED);
        }
        // 获取并增加计数
        AtomicInteger count = requestCountCache.get(key, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();
        if (currentCount > urlLimit.maxRequests()) {
            // 记录违规
            stats.setViolationCount(stats.getViolationCount() + 1);
            stats.setLastViolationTime(LocalDateTime.now());
            // 检查是否需要封禁
            if (stats.getViolationCount() >= urlLimit.maxRequests() + 100) {
                stats.setBlocked(true);
                throw new BaseException(MessageConstant.ACCESS_FREQUENCY_LIMIT_EXCEEDED);
            }
            statsCache.put(key, stats);
            throw new BaseException(MessageConstant.ACCESS_SPEED_LIMIT_EXCEEDED);
        }
        return joinPoint.proceed();
    }
}
