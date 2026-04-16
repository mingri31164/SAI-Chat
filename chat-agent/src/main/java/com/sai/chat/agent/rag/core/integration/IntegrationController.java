/*
 * Agent框架集成控制器
 */

package com.sai.chat.agent.rag.core.integration;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.rag.core.observe.eval.EvalCase;
import com.sai.chat.agent.rag.core.observe.metrics.MetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Agent框架集成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final AgentFrameworkIntegrator integrator;

    /**
     * 执行集成调用
     */
    @PostMapping("/execute")
    public Result<AgentFrameworkIntegrator.IntegratedResult> execute(
            @RequestBody AgentFrameworkIntegrator.IntegratedRequest request) {
        
        if (request.getRequestId() == null) {
            request.setRequestId("req-" + System.currentTimeMillis());
        }
        
        AgentFrameworkIntegrator.IntegratedResult result = integrator.execute(request);
        
        return new Result<AgentFrameworkIntegrator.IntegratedResult>()
                .setCode(Result.SUCCESS_CODE)
                .setData(result);
    }

    /**
     * 获取性能报告
     */
    @GetMapping("/performance")
    public Result<?> getPerformanceReport() {
        return new Result<>()
                .setCode(Result.SUCCESS_CODE)
                .setData(integrator.getPerformanceReport());
    }

    /**
     * 获取指标快照
     */
    @GetMapping("/metrics")
    public Result<MetricsCollector.MetricsSnapshot> getMetrics() {
        return new Result<MetricsCollector.MetricsSnapshot>()
                .setCode(Result.SUCCESS_CODE)
                .setData(integrator.getMetricsSnapshot());
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        MetricsCollector.MetricsSnapshot metrics = integrator.getMetricsSnapshot();
        
        Map<String, Object> health = Map.of(
                "status", metrics.getErrorRate() < 0.1 ? "healthy" : "degraded",
                "successRate", String.format("%.2f%%", metrics.getSuccessRate() * 100),
                "avgLatency", String.format("%.0fms", metrics.getAverageDurationMs()),
                "qps", String.format("%.2f", metrics.getQps()),
                "activeSessions", metrics.getActiveSessions()
        );
        
        return new Result<Map<String, Object>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(health);
    }
}