/*
 * 优化建议执行器
 * 
 * 执行性能优化建议并跟踪效果
 */

package com.sai.chat.agent.rag.core.observe.analysis;

import com.sai.chat.agent.rag.core.observe.analysis.PerformanceAnalyzer.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优化建议执行器
 */
@Slf4j
@Component
public class OptimizationExecutor {

    private final Map<String, OptimizationExperiment> experiments = new ConcurrentHashMap<>();
    private final Map<String, Double> baselineMetrics = new ConcurrentHashMap<>();

    /**
     * 创建优化实验
     */
    public OptimizationExperiment createExperiment(OptimizationSuggestion suggestion) {
        String experimentId = "exp-" + UUID.randomUUID().toString().substring(0, 8);
        
        OptimizationExperiment experiment = OptimizationExperiment.builder()
                .experimentId(experimentId)
                .suggestion(suggestion)
                .status(ExperimentStatus.PENDING)
                .createdTime(System.currentTimeMillis())
                .metricImprovements(new HashMap<>())
                .rolloutPercentage(0)
                .build();
        
        experiments.put(experimentId, experiment);
        
        log.info("Created optimization experiment: {} for suggestion: {}", 
                experimentId, suggestion.getTitle());
        
        return experiment;
    }

    /**
     * 启动实验
     */
    public OptimizationExperiment startExperiment(String experimentId, Map<String, Double> baseline) {
        OptimizationExperiment experiment = experiments.get(experimentId);
        if (experiment == null) {
            throw new IllegalArgumentException("Experiment not found: " + experimentId);
        }
        
        // 记录基线指标
        baselineMetrics.putAll(baseline);
        experiment.setBaselineMetrics(new HashMap<>(baseline));
        experiment.setStatus(ExperimentStatus.RUNNING);
        experiment.setStartTime(System.currentTimeMillis());
        
        log.info("Started experiment: {} with baseline: {}", experimentId, baseline);
        
        return experiment;
    }

    /**
     * 更新实验指标
     */
    public void updateExperimentMetrics(String experimentId, Map<String, Double> currentMetrics) {
        OptimizationExperiment experiment = experiments.get(experimentId);
        if (experiment == null || experiment.getStatus() != ExperimentStatus.RUNNING) {
            return;
        }
        
        Map<String, Double> improvements = new HashMap<>();
        Map<String, Double> baseline = experiment.getBaselineMetrics();
        
        for (Map.Entry<String, Double> entry : currentMetrics.entrySet()) {
            String metric = entry.getKey();
            Double current = entry.getValue();
            Double base = baseline.get(metric);
            
            if (base != null && base > 0) {
                // 计算改进百分比
                double improvement = (base - current) / base * 100;
                improvements.put(metric, improvement);
                
                // 检查是否达到目标
                OptimizationSuggestion suggestion = experiment.getSuggestion();
                if (isTargetAchieved(metric, current, suggestion)) {
                    completeExperiment(experimentId, true);
                    return;
                }
            }
        }
        
        experiment.setMetricImprovements(improvements);
    }

    /**
     * 完成实验
     */
    public OptimizationExperiment completeExperiment(String experimentId, boolean success) {
        OptimizationExperiment experiment = experiments.get(experimentId);
        if (experiment == null) {
            return null;
        }
        
        experiment.setStatus(success ? ExperimentStatus.COMPLETED : ExperimentStatus.FAILED);
        experiment.setEndTime(System.currentTimeMillis());
        
        if (success) {
            experiment.setRolloutPercentage(100);
            
            // 生成推广建议
            log.info("Experiment {} completed successfully with improvements: {}", 
                    experimentId, experiment.getMetricImprovements());
        } else {
            log.warn("Experiment {} failed", experimentId);
        }
        
        return experiment;
    }

    /**
     * 回滚实验
     */
    public OptimizationExperiment rollbackExperiment(String experimentId) {
        OptimizationExperiment experiment = experiments.get(experimentId);
        if (experiment == null) {
            return null;
        }
        
        experiment.setStatus(ExperimentStatus.ROLLED_BACK);
        experiment.setEndTime(System.currentTimeMillis());
        
        log.info("Rolled back experiment: {}", experimentId);
        
        return experiment;
    }

    /**
     * 获取实验状态
     */
    public OptimizationExperiment getExperiment(String experimentId) {
        return experiments.get(experimentId);
    }

    /**
     * 获取所有实验
     */
    public List<OptimizationExperiment> getAllExperiments() {
        return new ArrayList<>(experiments.values());
    }

    /**
     * 获取运行中的实验
     */
    public List<OptimizationExperiment> getRunningExperiments() {
        return experiments.values().stream()
                .filter(e -> e.getStatus() == ExperimentStatus.RUNNING)
                .toList();
    }

    /**
     * 渐进步骤推广
     */
    public OptimizationExperiment rolloutGradually(String experimentId, int percentage) {
        OptimizationExperiment experiment = experiments.get(experimentId);
        if (experiment == null) {
            return null;
        }
        
        if (experiment.getStatus() != ExperimentStatus.RUNNING) {
            throw new IllegalStateException("Can only rollout running experiments");
        }
        
        experiment.setRolloutPercentage(percentage);
        
        log.info("Rolling out experiment {} to {}%", experimentId, percentage);
        
        // 如果达到100%，标记为完成
        if (percentage >= 100) {
            return completeExperiment(experimentId, true);
        }
        
        return experiment;
    }

    /**
     * 生成优化报告
     */
    public OptimizationReport generateReport() {
        List<OptimizationExperiment> all = getAllExperiments();
        
        long completed = all.stream()
                .filter(e -> e.getStatus() == ExperimentStatus.COMPLETED)
                .count();
        
        long failed = all.stream()
                .filter(e -> e.getStatus() == ExperimentStatus.FAILED)
                .count();
        
        double avgImprovement = all.stream()
                .filter(e -> e.getStatus() == ExperimentStatus.COMPLETED)
                .flatMap(e -> e.getMetricImprovements().values().stream())
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        
        return OptimizationReport.builder()
                .generatedTime(System.currentTimeMillis())
                .totalExperiments(all.size())
                .completedExperiments((int) completed)
                .failedExperiments((int) failed)
                .runningExperiments(getRunningExperiments().size())
                .averageImprovement(avgImprovement)
                .experiments(all)
                .build();
    }

    private boolean isTargetAchieved(String metric, Double current, OptimizationSuggestion suggestion) {
        // 简化实现：假设任何改进超过20%就认为达到目标
        Double baseline = baselineMetrics.get(metric);
        if (baseline == null || baseline == 0) {
            return false;
        }
        
        double improvement = (baseline - current) / baseline * 100;
        return improvement >= 20;
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class OptimizationExperiment {
        private String experimentId;
        private OptimizationSuggestion suggestion;
        private ExperimentStatus status;
        private long createdTime;
        private long startTime;
        private long endTime;
        private Map<String, Double> baselineMetrics;
        private Map<String, Double> metricImprovements;
        private int rolloutPercentage;
    }

    @Data
    @Builder
    public static class OptimizationReport {
        private long generatedTime;
        private int totalExperiments;
        private int completedExperiments;
        private int failedExperiments;
        private int runningExperiments;
        private double averageImprovement;
        private List<OptimizationExperiment> experiments;
    }

    public enum ExperimentStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        ROLLED_BACK
    }
}
