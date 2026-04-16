/*
 * Agent框架集成器
 *
 * 将核心Agent与Phase 1-5的所有组件集成
 */

package com.sai.chat.agent.rag.core.integration;

import com.sai.chat.agent.rag.core.agent.AgentExecutor;
import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.observe.analysis.PerformanceAnalyzer;
import com.sai.chat.agent.rag.core.observe.eval.EvalRunner;
import com.sai.chat.agent.rag.core.observe.eval.EvalCase;
import com.sai.chat.agent.rag.core.observe.metrics.MetricsCollector;
import com.sai.chat.agent.rag.core.observe.prompt.PromptTuner;
import com.sai.chat.agent.rag.core.observe.review.ReviewAnalyzer;
import com.sai.chat.agent.rag.core.security.ContentSafetyFilter;
import com.sai.chat.agent.rag.core.security.PermissionValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent框架集成器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentFrameworkIntegrator {

    private final AgentExecutor agentExecutor;
    private final MetricsCollector metricsCollector;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final ReviewAnalyzer reviewAnalyzer;
    private final EvalRunner evalRunner;
    private final PromptTuner promptTuner;
    private final PermissionValidator permissionValidator;
    private final ContentSafetyFilter contentSafetyFilter;

    /**
     * 执行完整的集成Agent调用
     */
    public IntegratedResult execute(IntegratedRequest request) {
        long startTime = System.currentTimeMillis();
        IntegratedResult result = IntegratedResult.builder()
                .requestId(request.getRequestId())
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .startTime(startTime)
                .build();

        try {
            // 1. 权限验证
            PermissionValidator.PermissionContext permContext = PermissionValidator.PermissionContext.builder()
                    .userId(request.getUserId())
                    .permission("agent:invoke")
                    .resourceId(request.getSessionId())
                    .build();

            PermissionValidator.PermissionResult permResult = permissionValidator.validate(permContext);
            if (!permResult.isAllowed()) {
                result.setSuccess(false);
                result.setErrorMessage("权限验证失败: " + permResult.getReason());
                result.setEndTime(System.currentTimeMillis());
                result.setDurationMs(result.getEndTime() - startTime);
                return result;
            }

            // 2. 内容安全检查
            ContentSafetyFilter.SafetyResult safetyResult =
                    contentSafetyFilter.filterInput(request.getUserInput(), request.getUserId());
            if (!safetyResult.isAllowed()) {
                result.setSuccess(false);
                result.setErrorMessage("内容安全检查未通过");
                result.setEndTime(System.currentTimeMillis());
                result.setDurationMs(result.getEndTime() - startTime);
                return result;
            }

            // 3. 记录请求开始
            metricsCollector.recordRequestStart(request.getSessionId(), request.getUserId());

            // 4. 执行Agent
            AgentRequest agentRequest = AgentRequest.builder()
                    .sessionId(request.getSessionId())
                    .userId(request.getUserId())
                    .question(request.getUserInput())
                    .build();

            AgentResponse agentResponse = agentExecutor.execute(agentRequest);

            // 5. 记录请求结束
            long durationMs = System.currentTimeMillis() - startTime;
            metricsCollector.recordRequestEnd(request.getSessionId(), agentResponse, durationMs);

            // 构建结果
            result.setSuccess(agentResponse.isSuccess());
            result.setAgentResponse(agentResponse);
            result.setSafetyViolations(safetyResult.getViolations());
            result.setEndTime(System.currentTimeMillis());
            result.setDurationMs(durationMs);

            log.info("Integrated execution completed: requestId={}, duration={}ms, success={}",
                    request.getRequestId(), result.getDurationMs(), result.isSuccess());

        } catch (Exception e) {
            log.error("Integrated execution failed: requestId={}", request.getRequestId(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setEndTime(System.currentTimeMillis());
            result.setDurationMs(result.getEndTime() - startTime);
        }

        return result;
    }

    /**
     * 获取性能报告
     */
    public PerformanceAnalyzer.PerformanceReport getPerformanceReport() {
        return performanceAnalyzer.analyze();
    }

    /**
     * 获取指标快照
     */
    public MetricsCollector.MetricsSnapshot getMetricsSnapshot() {
        return metricsCollector.getGlobalSnapshot();
    }

    // ==================== 数据类 ====================

    @Data
    @lombok.Builder
    public static class IntegratedRequest {
        private String requestId;
        private String sessionId;
        private String userId;
        private String userInput;
        private boolean autoEval;
        private List<EvalCase> evalCases;
    }

    @Data
    @lombok.Builder
    public static class IntegratedResult {
        private String requestId;
        private String sessionId;
        private String userId;
        private boolean success;
        private String errorMessage;
        private AgentResponse agentResponse;
        private long startTime;
        private long endTime;
        private long durationMs;
        private List<ContentSafetyFilter.Violation> safetyViolations;
    }
}