/*
 * Agent 追踪回调实现
 */

package com.sai.chat.agent.rag.core.observe.trace;

import com.sai.chat.agent.rag.core.agent.AgentCallback;
import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import com.sai.chat.agent.rag.core.agent.state.ToolCallRecord;
import com.sai.chat.agent.rag.core.agent.state.ToolCallResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 追踪回调实现
 */
@Slf4j
public class TraceCallback implements AgentCallback {

    private final TraceContextHolder.TraceContext context;

    public TraceCallback(TraceContextHolder.TraceContext context) {
        this.context = context;
    }

    @Override
    public void onStatusChange(AgentStatus status, String message) {
        context.addEvent("status_change: " + status + " - " + message,
                AgentTrace.TraceEvent.EventType.STATUS_CHANGED);
    }

    @Override
    public void onThought(String thought, int step) {
        context.addEvent("iteration_" + step + "_thought: " + truncate(thought, 200),
                AgentTrace.TraceEvent.EventType.THINKING_COMPLETED);
    }

    @Override
    public void onToolCallStart(ToolCallRecord record) {
        context.addEvent("tool_start: " + record.getToolName(),
                AgentTrace.TraceEvent.EventType.TOOL_CALL_STARTED);
    }

    @Override
    public void onToolCallEnd(ToolCallRecord record) {
        ToolCallResult result = record.getResult();
        String summary = result != null ? result.getSummary() : "null";
        context.addEvent("tool_end: " + record.getToolName() + " - " + truncate(summary, 200),
                AgentTrace.TraceEvent.EventType.TOOL_CALL_COMPLETED);
    }

    @Override
    public void onComplete(boolean success, String status, String finalAnswer, int iterations, int tokens) {
        context.addEvent("agent_complete: success=" + success + ", status=" + status + ", iterations=" + iterations + ", tokens=" + tokens,
                success ? AgentTrace.TraceEvent.EventType.AGENT_COMPLETED : AgentTrace.TraceEvent.EventType.AGENT_FAILED);
    }

    @Override
    public void onError(String error, boolean canRetry) {
        context.recordError(error, null);
        context.addEvent("error: " + error + ", canRetry=" + canRetry,
                AgentTrace.TraceEvent.EventType.ERROR_OCCURRED);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }
}
