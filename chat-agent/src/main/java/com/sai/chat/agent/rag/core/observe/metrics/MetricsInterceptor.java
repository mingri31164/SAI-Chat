/*
 * 指标拦截器
 */

package com.sai.chat.agent.rag.core.observe.metrics;

import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 指标拦截器
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricsInterceptor {

    private final MetricsCollector metricsCollector;
    private final ObserveProperties observeProperties;

    @Pointcut("execution(* com.sai.chat.agent.rag.core.agent.AgentService.execute(..))")
    public void agentExecution() {
    }

    @Around("agentExecution()")
    public Object recordMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!observeProperties.isMetricsEnabled()) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        String sessionId = extractSessionId(args);
        String userId = extractUserId(args);

        long startTime = System.currentTimeMillis();
        metricsCollector.recordRequestStart(sessionId, userId);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (result != null && result instanceof AgentResponse) {
                AgentResponse response = (AgentResponse) result;
                metricsCollector.recordRequestEnd(sessionId, response, duration);
            }

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            AgentResponse errorResponse = AgentResponse.builder()
                    .success(false)
                    .status(AgentStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            metricsCollector.recordRequestEnd(sessionId, errorResponse, duration);
            throw e;
        }
    }

    private String extractSessionId(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof com.sai.chat.agent.rag.core.agent.request.AgentRequest request) {
                return request.getSessionId();
            }
        }
        return null;
    }

    private String extractUserId(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof com.sai.chat.agent.rag.core.agent.request.AgentRequest request) {
                return request.getUserId();
            }
        }
        return null;
    }
}
