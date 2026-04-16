/*
 * Licensed to the Apache Software.  Foundation (ASF) under one or more
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

package com.sai.chat.agent.rag.core.observe.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评测报告
 * <p>
 * 汇总所有评测用例的执行结果和统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvalReport {

    /**
     * 报告ID
     */
    private String reportId;

    /**
     * 报告生成时间
     */
    private long timestamp;

    /**
     * 评测开始时间
     */
    private long startTime;

    /**
     * 评测结束时间
     */
    private long endTime;

    /**
     * 总耗时(ms)
     */
    private long totalDurationMs;

    /**
     * 用例总数
     */
    private int totalCases;

    /**
     * 通过数
     */
    private int passedCases;

    /**
     * 失败数
     */
    private int failedCases;

    /**
     * 通过率
     */
    private double passRate;

    /**
     * 综合得分
     */
    private double overallScore;

    /**
     * 各维度得分
     */
    @Builder.Default
    private Map<String, Double> dimensionScores = Map.of();

    /**
     * 平均响应时间
     */
    private double averageDurationMs;

    /**
     * 平均迭代次数
     */
    private double averageIterations;

    /**
     * 各用例结果
     */
    @Builder.Default
    private List<EvalResult> results = new ArrayList<>();

    /**
     * 按标签统计
     */
    @Builder.Default
    private Map<String, TagStatistics> tagStatistics = Map.of();

    /**
     * 按难度统计
     */
    @Builder.Default
    private Map<EvalCase.DifficultyLevel, DifficultyStatistics> difficultyStatistics = Map.of();

    /**
     * 失败用例列表
     */
    @Builder.Default
    private List<EvalResult> failedResults = new ArrayList<>();

    /**
     * 性能统计
     */
    private PerformanceStats performanceStats;

    /**
     * 报告摘要
     */
    private String summary;

    // ==================== 统计方法 ====================

    /**
     * 按标签统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagStatistics {
        private String tag;
        private int total;
        private int passed;
        private double passRate;
        private double averageScore;

        public static TagStatistics fromResults(String tag, List<EvalResult> results) {
            List<EvalResult> filtered = results.stream()
                    .filter(r -> r.getTags() != null && r.getTags().contains(tag))
                    .toList();
            if (filtered.isEmpty()) {
                return TagStatistics.builder()
                        .tag(tag)
                        .total(0)
                        .passed(0)
                        .passRate(0)
                        .averageScore(0)
                        .build();
            }
            long passCount = filtered.stream().filter(EvalResult::isPassed).count();
            double avgScore = filtered.stream()
                    .mapToDouble(EvalResult::getScore)
                    .average()
                    .orElse(0);

            return TagStatistics.builder()
                    .tag(tag)
                    .total(filtered.size())
                    .passed((int) passCount)
                    .passRate((double) passCount / filtered.size())
                    .averageScore(avgScore)
                    .build();
        }
    }

    /**
     * 按难度统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyStatistics {
        private EvalCase.DifficultyLevel level;
        private int total;
        private int passed;
        private double passRate;
        private double averageScore;
        private double averageDurationMs;
    }

    /**
     * 性能统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceStats {
        private double minDurationMs;
        private double maxDurationMs;
        private double avgDurationMs;
        private double p50DurationMs;
        private double p95DurationMs;
        private double p99DurationMs;
        private long totalTokens;
        private double totalCostYuan;
        private int totalToolCalls;
    }

    // ==================== 报告生成 ====================

    /**
     * 从结果列表生成报告
     */
    public static EvalReport fromResults(String reportId, List<EvalResult> results,
                                        long startTime, long endTime) {
        if (results == null || results.isEmpty()) {
            return emptyReport(reportId, startTime, endTime);
        }

        int total = results.size();
        int passed = (int) results.stream().filter(EvalResult::isPassed).count();
        double overallScore = results.stream()
                .mapToDouble(EvalResult::getScore)
                .average()
                .orElse(0);

        double avgDuration = results.stream()
                .mapToLong(EvalResult::getDurationMs)
                .average()
                .orElse(0);

        double avgIterations = results.stream()
                .mapToInt(EvalResult::getIterations)
                .average()
                .orElse(0);

        // 按标签统计
        Map<String, List<EvalResult>> byTag = results.stream()
                .filter(r -> r.getTags() != null)
                .flatMap(r -> r.getTags().stream().map(tag -> Map.entry(tag, r)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        Map<String, TagStatistics> tagStats = byTag.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> TagStatistics.fromResults(e.getKey(), e.getValue())
                ));

        // 失败用例
        List<EvalResult> failed = results.stream()
                .filter(r -> !r.isPassed())
                .toList();

        // 性能统计
        long[] durations = results.stream()
                .mapToLong(EvalResult::getDurationMs)
                .sorted()
                .toArray();

        PerformanceStats perfStats = PerformanceStats.builder()
                .minDurationMs(durations.length > 0 ? durations[0] : 0)
                .maxDurationMs(durations.length > 0 ? durations[durations.length - 1] : 0)
                .avgDurationMs(avgDuration)
                .p50DurationMs(percentile(durations, 0.5))
                .p95DurationMs(percentile(durations, 0.95))
                .p99DurationMs(percentile(durations, 0.99))
                .build();

        // 生成摘要
        String summary = generateSummary(total, passed, overallScore, avgDuration);

        return EvalReport.builder()
                .reportId(reportId)
                .timestamp(System.currentTimeMillis())
                .startTime(startTime)
                .endTime(endTime)
                .totalDurationMs(endTime - startTime)
                .totalCases(total)
                .passedCases(passed)
                .failedCases(total - passed)
                .passRate((double) passed / total)
                .overallScore(overallScore)
                .averageDurationMs(avgDuration)
                .averageIterations(avgIterations)
                .results(results)
                .tagStatistics(tagStats)
                .failedResults(failed)
                .performanceStats(perfStats)
                .summary(summary)
                .build();
    }

    private static EvalReport emptyReport(String reportId, long startTime, long endTime) {
        return EvalReport.builder()
                .reportId(reportId)
                .timestamp(System.currentTimeMillis())
                .startTime(startTime)
                .endTime(endTime)
                .totalDurationMs(endTime - startTime)
                .totalCases(0)
                .passedCases(0)
                .failedCases(0)
                .passRate(0)
                .overallScore(0)
                .summary("No test cases executed")
                .build();
    }

    private static double percentile(long[] sorted, double p) {
        if (sorted == null || sorted.length == 0) return 0;
        int index = (int) Math.ceil(p * sorted.length) - 1;
        index = Math.max(0, Math.min(index, sorted.length - 1));
        return sorted[index];
    }

    private static String generateSummary(int total, int passed, double score, double avgDuration) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("评测完成: %d/%d 通过 (%.1f%%), 平均得分 %.1f, 平均耗时 %.0fms",
                passed, total, (double) passed / total * 100, score, avgDuration));

        if (passed == total) {
            sb.append(" - 全部通过!");
        } else if (passed > total * 0.8) {
            sb.append(" - 表现良好");
        } else if (passed > total * 0.6) {
            sb.append(" - 需要改进");
        } else {
            sb.append(" - 表现较差，建议优化");
        }

        return sb.toString();
    }
}
