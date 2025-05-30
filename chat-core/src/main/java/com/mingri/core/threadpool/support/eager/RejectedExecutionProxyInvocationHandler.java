package com.mingri.core.threadpool.support.eager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * 拒绝策略代理
 */
@Slf4j
@AllArgsConstructor
public class RejectedExecutionProxyInvocationHandler implements InvocationHandler {

    private RejectedExecutionHandler target;

    private EagerThreadPoolExecutor executor;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 执行拒绝策略前自增拒绝次数 & 发起报警
        executor.incrementRejectCount();
        // TODO 后续接入飞书预警
        log.error("线程池执行拒绝策略{}次, 此处模拟报警...", executor.getRejectCount());
        return method.invoke(target, args);
    }
}
