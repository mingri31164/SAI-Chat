package com.mingri.core.Idempotent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class IdempotentAspect {

    @Autowired
    private RedissonLock redissonLock;

    @Autowired
    private IdempotentKeyGenerator keyGenerator;

    @Autowired
    private IdempotentResultStore resultStore;

    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(Idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent Idempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        Object[] args = pjp.getArgs();

        String keySuffix = method.getDeclaringClass().getName() + ":" + method.getName() + ":" +
                keyGenerator.generateKey(method, args, Idempotent.key(), paramNames);

        String lockKey = "lock:" + keySuffix;
        String resultKey = "idem:" + keySuffix;

        String cachedResult = resultStore.getResult(resultKey, String.class);
        if (cachedResult != null) {
            return resultStore.deserializeResult(cachedResult, method.getReturnType());
        }

        boolean locked = false;
        try {
            locked = redissonLock.tryLock(lockKey, Idempotent.expire());
            if (!locked) {
                // 锁获取失败，短暂sleep后再次重查缓存
                Thread.sleep(50);
                cachedResult = resultStore.getResult(resultKey, String.class);
                if (cachedResult != null) {
                    return resultStore.deserializeResult(cachedResult, method.getReturnType());
                }
                throw new RuntimeException("请勿重复操作");
            }

            // 拿到锁后再查一次缓存，防止锁等待期间其它线程已写入
            cachedResult = resultStore.getResult(resultKey, String.class);
            if (cachedResult != null) {
                return resultStore.deserializeResult(cachedResult, method.getReturnType());
            }

            Object result = pjp.proceed();

            // 先写缓存再释放锁，保证其它线程能读到结果
            if (result != null) {
                resultStore.saveResult(resultKey, result, Idempotent.expire());
            }
            return result;
        } catch (Exception e) {
            // 业务异常时不写缓存，保证后续请求能重新执行业务
            throw e;
        } finally {
            if (locked) {
                redissonLock.unlock(lockKey);
            }
        }
    }
}