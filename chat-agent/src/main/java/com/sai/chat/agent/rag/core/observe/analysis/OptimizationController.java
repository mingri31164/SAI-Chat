/*
 * 优化分析控制器
 */

package com.sai.chat.agent.rag.core.observe.analysis;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.rag.core.observe.analysis.OptimizationExecutor.*;
import com.sai.chat.agent.rag.core.observe.analysis.PerformanceAnalyzer.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 优化分析API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/optimize")
@RequiredArgsConstructor
public class OptimizationController {

    private final PerformanceAnalyzer performanceAnalyzer;
    private final OptimizationExecutor optimizationExecutor;

    /**
     * 执行性能分析
     */
    @GetMapping("/analyze")
    public Result<PerformanceReport> analyzePerformance(
            @RequestParam(required = false) Long since) {
        
        PerformanceReport report;
        if (since != null) {
            report = performanceAnalyzer.analyze(since);
        } else {
            report = performanceAnalyzer.analyze();
        }
        
        return new Result<PerformanceReport>()
                .setCode(Result.SUCCESS_CODE)
                .setData(report);
    }

    /**
     * 分析单个追踪的性能
     */
    @GetMapping("/trace/{traceId}/analyze")
    public Result<TracePerformanceAnalysis> analyzeTrace(@PathVariable String traceId) {
        // 从追踪查询服务获取追踪
        // 这里简化实现，实际应从TraceQueryService获取
        TracePerformanceAnalysis analysis = TracePerformanceAnalysis.builder()
                .traceId(traceId)
                .phaseDurations(Map.of())
                .bottlenecks(List.of())
                .issues(List.of())
                .build();
        
        return new Result<TracePerformanceAnalysis>()
                .setCode(Result.SUCCESS_CODE)
                .setData(analysis);
    }

    /**
     * 获取健康状态
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> getHealthStatus() {
        PerformanceReport report = performanceAnalyzer.analyze();
        
        return new Result<Map<String, Object>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(Map.of(
                        "status", report.getHealthStatus(),
                        "score", report.getOverallScore(),
                        "issues", report.getIssues().size(),
                        "timestamp", report.getTimestamp()
                ));
    }

    /**
     * 创建优化实验
     */
    @PostMapping("/experiment")
    public Result<OptimizationExperiment> createExperiment(@RequestBody OptimizationSuggestion suggestion) {
        OptimizationExperiment experiment = optimizationExecutor.createExperiment(suggestion);
        
        return new Result<OptimizationExperiment>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiment);
    }

    /**
     * 启动实验
     */
    @PostMapping("/experiment/{experimentId}/start")
    public Result<OptimizationExperiment> startExperiment(
            @PathVariable String experimentId,
            @RequestBody Map<String, Double> baseline) {
        
        OptimizationExperiment experiment = optimizationExecutor.startExperiment(experimentId, baseline);
        
        return new Result<OptimizationExperiment>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiment);
    }

    /**
     * 更新实验指标
     */
    @PostMapping("/experiment/{experimentId}/metrics")
    public Result<Void> updateMetrics(
            @PathVariable String experimentId,
            @RequestBody Map<String, Double> metrics) {
        
        optimizationExecutor.updateExperimentMetrics(experimentId, metrics);
        
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE);
    }

    /**
     * 完成实验
     */
    @PostMapping("/experiment/{experimentId}/complete")
    public Result<OptimizationExperiment> completeExperiment(
            @PathVariable String experimentId,
            @RequestParam(defaultValue = "true") boolean success) {
        
        OptimizationExperiment experiment = optimizationExecutor.completeExperiment(experimentId, success);
        
        return new Result<OptimizationExperiment>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiment);
    }

    /**
     * 回滚实验
     */
    @PostMapping("/experiment/{experimentId}/rollback")
    public Result<OptimizationExperiment> rollbackExperiment(@PathVariable String experimentId) {
        OptimizationExperiment experiment = optimizationExecutor.rollbackExperiment(experimentId);
        
        return new Result<OptimizationExperiment>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiment);
    }

    /**
     * 渐进步骤推广
     */
    @PostMapping("/experiment/{experimentId}/rollout")
    public Result<OptimizationExperiment> rolloutExperiment(
            @PathVariable String experimentId,
            @RequestParam int percentage) {
        
        OptimizationExperiment experiment = optimizationExecutor.rolloutGradually(experimentId, percentage);
        
        return new Result<OptimizationExperiment>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiment);
    }

    /**
     * 获取实验详情
     */
    @GetMapping("/experiment/{experimentId}")
    public Result<OptimizationExperiment> getExperiment(@PathVariable String experimentId) {
        OptimizationExperiment experiment = optimizationExecutor.getExperiment(experimentId);
        
        return new Result<OptimizationExperiment>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiment);
    }

    /**
     * 获取所有实验
     */
    @GetMapping("/experiments")
    public Result<List<OptimizationExperiment>> getAllExperiments() {
        List<OptimizationExperiment> experiments = optimizationExecutor.getAllExperiments();
        
        return new Result<List<OptimizationExperiment>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiments);
    }

    /**
     * 获取运行中的实验
     */
    @GetMapping("/experiments/running")
    public Result<List<OptimizationExperiment>> getRunningExperiments() {
        List<OptimizationExperiment> experiments = optimizationExecutor.getRunningExperiments();
        
        return new Result<List<OptimizationExperiment>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(experiments);
    }

    /**
     * 获取优化报告
     */
    @GetMapping("/report")
    public Result<OptimizationReport> getOptimizationReport() {
        OptimizationReport report = optimizationExecutor.generateReport();
        
        return new Result<OptimizationReport>()
                .setCode(Result.SUCCESS_CODE)
                .setData(report);
    }
}
