/*
 * 追踪拦截的执行器
 */

package com.sai.chat.agent.rag.core.observe.trace;

import com.sai.chat.agent.rag.core.agent.AgentCallback;
import com.sai.chat.agent.rag.core.agent.AgentExecutor;
import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import com.sai.chat.agent.rag.core.agent.state.ToolCallRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 追踪拦截的执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TracedAgentExecutor implements AgentExecutor {

    private final AgentExecutor delegate;

    @Override
    public AgentResponse execute(AgentRequest request) {
        TraceContextHolder.TraceContext context = TraceContextHolder.getContext();

        if (context == null) {
            return delegate.execute(request);
        }

        TraceCallback traceCallback = new TraceCallback(context);
        AgentCallback wrappedCallback = wrapCallback(traceCallback);

        try {
            context.addEvent("executor_start", AgentTrace.TraceEvent.EventType.ITERATION_STARTED);
            return delegate.executeStream(request, wrappedCallback);
        } finally {
            context.addEvent("executor_end", AgentTrace.TraceEvent.EventType.ITERATION_COMPLETED);
        }
    }

    @Override
    public AgentResponse executeStream(AgentRequest request, AgentCallback callback) {
        TraceContextHolder.TraceContext context = TraceContextHolder.getContext();

        if (context == null) {
            return delegate.executeStream(request, callback);
        }

        TraceCallback traceCallback = new TraceCallback(context);
        AgentCallback combinedCallback = combineCallbacks(traceCallback, callback);

        try {
            return delegate.executeStream(request, combinedCallback);
        } catch (Exception e) {
            context.recordError(e.getMessage(), e);
            context.addEvent("executor_error: " + e.getMessage(), AgentTrace.TraceEvent.EventType.ERROR_OCCURRED);
            throw e;
        }
    }

    @Override
    public String getName() {
        return "Traced" + delegate.getName();
    }

    @Override
    public String getDescription() {
        return "Traced " + delegate.getDescription();
    }

    private AgentCallback wrapCallback(AgentCallback traceCallback) {
        return new AgentCallback() {
            @Override
            public void onStatusChange(AgentStatus status, String message) {
                traceCallback.onStatusChange(status, message);
            }

            @Override
            public void onThought(String thought, int step) {
                traceCallback.onThought(thought, step);
            }

            @Override
            public void onToolCallStart(ToolCallRecord record) {
                traceCallback.onToolCallStart(record);
            }

            @Override
            public void onToolCallEnd(ToolCallRecord record) {
                traceCallback.onToolCallEnd(record);
            }

            @Override
            public void onComplete(boolean success, String status, String finalAnswer, int iterations, int tokens) {
                traceCallback.onComplete(success, status, finalAnswer, iterations, tokens);
            }

            @Override
            public void onReasoningContent(String content) {
                // tracing does not need reasoning content
            }

            @Override
            public void onParsedResult(String actionType, String finalAnswer, String waitMessage, String thought) {
                // tracing does not need parsed result
            }

            @Override
            public void onError(String error, boolean canRetry) {
                traceCallback.onError(error, canRetry);
            }

            @Override
            public void onAnswerContent(String content) {
                traceCallback.onAnswerContent(content);
            }
        };
    }

    private AgentCallback combineCallbacks(AgentCallback first, AgentCallback second) {
        if (second == null) {
            return first;
        }

        return new AgentCallback() {
            @Override
            public void onStatusChange(AgentStatus status, String message) {
                first.onStatusChange(status, message);
                second.onStatusChange(status, message);
            }

            @Override
            public void onThought(String thought, int step) {
                first.onThought(thought, step);
                second.onThought(thought, step);
            }

            @Override
            public void onToolCallStart(ToolCallRecord record) {
                first.onToolCallStart(record);
                second.onToolCallStart(record);
            }

            @Override
            public void onToolCallEnd(ToolCallRecord record) {
                first.onToolCallEnd(record);
                second.onToolCallEnd(record);
            }

            @Override
            public void onComplete(boolean success, String status, String finalAnswer, int iterations, int tokens) {
                first.onComplete(success, status, finalAnswer, iterations, tokens);
                second.onComplete(success, status, finalAnswer, iterations, tokens);
            }

            @Override
            public void onError(String error, boolean canRetry) {
                first.onError(error, canRetry);
                second.onError(error, canRetry);
            }

            @Override
            public void onAnswerContent(String content) {
                first.onAnswerContent(content);
                second.onAnswerContent(content);
            }

            @Override
            public void onReasoningContent(String content) {
                first.onReasoningContent(content);
                second.onReasoningContent(content);
            }

            @Override
            public void onParsedResult(String actionType, String finalAnswer, String waitMessage, String thought) {
                first.onParsedResult(actionType, finalAnswer, waitMessage, thought);
                second.onParsedResult(actionType, finalAnswer, waitMessage, thought);
            }
        };
    }
}
