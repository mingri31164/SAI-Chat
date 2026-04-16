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

package com.sai.chat.agent.rag.core.observe.trace;

import com.sai.chat.agent.rag.core.agent.AgentCallback;
import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 追踪切面 - 使用AOP自动追踪Agent执行
 * <p>
 * 自动在Agent执行前后创建和管理追踪上下文
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(1)
public class TraceAspect {

    private final TraceRecorder traceRecorder;

    /**
     * 追踪开关
     */
    private boolean enabled = true;

    /**
     * Agent执行切入点
     */
    @Pointcut("execution(* com.sai.chat.agent.rag.core.agent.AgentService.execute(..))")
    public void agentExecution() {
    }

    /**
     * 环绕通知 - 追踪Agent执行
     */
    @Around("agentExecution()")
    public Object traceAgentExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enabled) {
            return joinPoint.proceed();
        }

        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        AgentRequest request = extractRequest(args);

        if (request == null) {
            return joinPoint.proceed();
        }

        // 创建追踪上下文
        TraceContextHolder.TraceContext context = TraceContextHolder.createNew(
                request.getSessionId(),
                request.getUserId()
        );

        // 创建根跨度
        AgentTrace.Span rootSpan = context.createRootSpan(
                "AgentExecution",
                AgentTrace.Span.SpanType.OTHER
        );
        rootSpan.setAttribute("question", truncate(request.getQuestion(), 500));
        rootSpan.setAttribute("mode", request.getMode() != null ? request.getMode().name() : "REACT");

        context.addEvent("agent_started: " + request.getQuestion(),
                AgentTrace.TraceEvent.EventType.AGENT_STARTED);

        try {
            // 继续执行
            Object result = joinPoint.proceed();

            // 处理结果
            if (result instanceof AgentResponse response) {
                if (response.isSuccess()) {
                    rootSpan.setStatus(AgentTrace.Span.SpanStatus.SUCCESS);
                    context.addEvent("agent_completed: success",
                            AgentTrace.TraceEvent.EventType.AGENT_COMPLETED);
                } else {
                    rootSpan.setStatus(AgentTrace.Span.SpanStatus.FAILED);
                    rootSpan.setError(response.getErrorMessage());
                    context.addEvent("agent_failed: " + response.getErrorMessage(),
                            AgentTrace.TraceEvent.EventType.AGENT_FAILED);
                }

                // 添加结果属性
                rootSpan.setAttribute("status", response.getStatus().name());
                rootSpan.setAttribute("iterations", String.valueOf(response.getTotalIterations()));
                rootSpan.setAttribute("duration_ms", String.valueOf(response.getDurationMs()));
                rootSpan.setAttribute("tokens", String.valueOf(response.getTotalTokens()));
            }

            // 结束跨度
            rootSpan.complete();
            context.setCurrentSpan(null);

            // 记录追踪
            AgentTrace trace = context.toAgentTrace();
            traceRecorder.record(trace);

            return result;

        } catch (Throwable e) {
            // 处理异常
            rootSpan.setStatus(AgentTrace.Span.SpanStatus.FAILED);
            rootSpan.setError(e.getMessage());
            rootSpan.complete();

            context.recordError(e.getMessage(), e);
            context.addEvent("agent_exception: " + e.getMessage(),
                    AgentTrace.TraceEvent.EventType.AGENT_FAILED);

            // 记录追踪
            AgentTrace trace = context.toAgentTrace();
            traceRecorder.record(trace);

            throw e;
        } finally {
            // 清理ThreadLocal
            TraceContextHolder.clear();
        }
    }

    /**
     * 从参数中提取AgentRequest
     */
    private AgentRequest extractRequest(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof AgentRequest request) {
                return request;
            }
        }
        return null;
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }

    /**
     * 启用追踪
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * 禁用追踪
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return this.enabled;
    }
}
