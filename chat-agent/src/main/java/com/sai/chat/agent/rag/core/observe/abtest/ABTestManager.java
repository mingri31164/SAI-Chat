/*
 * A/B测试管理器
 */

package com.sai.chat.agent.rag.core.observe.abtest;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A/B测试管理器
 */
@Slf4j
@Component
public class ABTestManager {

    private final Map<String, ABTest> activeTests = new ConcurrentHashMap<>();
    private final Map<String, List<ABVariant>> testVariants = new ConcurrentHashMap<>();
    private final Map<String, Map<String, VariantMetrics>> variantMetrics = new ConcurrentHashMap<>();
    private final Map<String, UserAssignment> userAssignments = new ConcurrentHashMap<>();

    /**
     * 创建A/B测试
     */
    public ABTest createTest(ABTest test) {
        test.setTestId("test-" + UUID.randomUUID().toString().substring(0, 8));
        test.setStatus(TestStatus.DRAFT);
        test.setCreatedTime(System.currentTimeMillis());
        
        activeTests.put(test.getTestId(), test);
        
        // 初始化变体
        List<ABVariant> variants = new ArrayList<>();
        for (int i = 0; i < test.getVariantCount(); i++) {
            ABVariant variant = ABVariant.builder()
                    .variantId(test.getTestId() + "-v" + i)
                    .variantName(i == 0 ? "control" : "treatment-" + i)
                    .weight(test.getVariantWeights()[i])
                    .isControl(i == 0)
                    .build();
            variants.add(variant);
        }
        testVariants.put(test.getTestId(), variants);
        variantMetrics.put(test.getTestId(), new HashMap<>());
        
        log.info("Created A/B test: {} with {} variants", test.getTestId(), test.getVariantCount());
        
        return test;
    }

    /**
     * 启动测试
     */
    public ABTest startTest(String testId) {
        ABTest test = activeTests.get(testId);
        if (test == null) {
            throw new IllegalArgumentException("Test not found: " + testId);
        }
        
        if (test.getStatus() != TestStatus.DRAFT && test.getStatus() != TestStatus.PAUSED) {
            throw new IllegalStateException("Cannot start test in status: " + test.getStatus());
        }
        
        test.setStatus(TestStatus.RUNNING);
        test.setStartTime(System.currentTimeMillis());
        
        log.info("Started A/B test: {}", testId);
        
        return test;
    }

    /**
     * 暂停测试
     */
    public ABTest pauseTest(String testId) {
        ABTest test = activeTests.get(testId);
        if (test == null) {
            throw new IllegalArgumentException("Test not found: " + testId);
        }
        
        test.setStatus(TestStatus.PAUSED);
        
        log.info("Paused A/B test: {}", testId);
        
        return test;
    }

    /**
     * 结束测试
     */
    public ABTest endTest(String testId, boolean markWinner) {
        ABTest test = activeTests.get(testId);
        if (test == null) {
            throw new IllegalArgumentException("Test not found: " + testId);
        }
        
        test.setStatus(TestStatus.COMPLETED);
        test.setEndTime(System.currentTimeMillis());
        
        if (markWinner) {
            determineWinner(testId);
        }
        
        log.info("Ended A/B test: {}", testId);
        
        return test;
    }

    /**
     * 为用户分配变体
     */
    public String assignVariant(String testId, String userId) {
        // 检查是否已有分配
        String assignmentKey = testId + ":" + userId;
        UserAssignment existing = userAssignments.get(assignmentKey);
        if (existing != null) {
            return existing.getVariantId();
        }
        
        // 获取测试配置
        ABTest test = activeTests.get(testId);
        if (test == null) {
            throw new IllegalArgumentException("Test not found: " + testId);
        }
        
        // 使用一致性哈希确保同一用户始终分配到同一变体
        int bucket = Math.abs((testId + userId).hashCode() % 100);
        
        List<ABVariant> variants = testVariants.get(testId);
        if (variants == null || variants.isEmpty()) {
            throw new IllegalStateException("No variants for test: " + testId);
        }
        
        // 按权重分配
        int cumulativeWeight = 0;
        String selectedVariantId = variants.get(0).getVariantId();
        
        for (ABVariant variant : variants) {
            cumulativeWeight += (int) (variant.getWeight() * 100);
            if (bucket < cumulativeWeight) {
                selectedVariantId = variant.getVariantId();
                break;
            }
        }
        
        // 记录分配
        UserAssignment assignment = UserAssignment.builder()
                .testId(testId)
                .userId(userId)
                .variantId(selectedVariantId)
                .assignedTime(System.currentTimeMillis())
                .build();
        
        userAssignments.put(assignmentKey, assignment);
        
        return selectedVariantId;
    }

    /**
     * 记录变体指标
     */
    public void recordMetric(String testId, String variantId, MetricType metricType, double value) {
        Map<String, VariantMetrics> metrics = variantMetrics.get(testId);
        if (metrics == null) {
            metrics = new HashMap<>();
            variantMetrics.put(testId, metrics);
        }
        
        VariantMetrics variantMetric = metrics.computeIfAbsent(variantId, k -> VariantMetrics.builder()
                .variantId(variantId)
                .impressions(0)
                .conversions(0)
                .totalRevenue(0)
                .totalLatency(0)
                .latencyCount(0)
                .errors(0)
                .build());
        
        switch (metricType) {
            case IMPRESSION -> variantMetric.incrementImpressions();
            case CONVERSION -> variantMetric.incrementConversions();
            case REVENUE -> variantMetric.addRevenue(value);
            case LATENCY -> variantMetric.recordLatency(value);
            case ERROR -> variantMetric.incrementErrors();
        }
        
        log.debug("Recorded metric for test:{} variant:{} type:{} value:{}", 
                testId, variantId, metricType, value);
    }

    /**
     * 获取测试结果
     */
    public TestResult getTestResult(String testId) {
        ABTest test = activeTests.get(testId);
        if (test == null) {
            throw new IllegalArgumentException("Test not found: " + testId);
        }
        
        List<ABVariant> variants = testVariants.get(testId);
        Map<String, VariantMetrics> metrics = variantMetrics.get(testId);
        
        List<VariantResult> variantResults = new ArrayList<>();
        
        for (ABVariant variant : variants) {
            VariantMetrics vm = metrics != null ? metrics.get(variant.getVariantId()) : null;
            VariantResult result = calculateVariantResult(variant, vm);
            variantResults.add(result);
        }
        
        // 计算统计显著性
        double significance = calculateSignificance(variantResults);
        
        // 确定获胜者
        String winnerId = null;
        double winnerConversion = 0;
        for (VariantResult vr : variantResults) {
            if (!vr.isControl && vr.getConversionRate() > winnerConversion) {
                winnerConversion = vr.getConversionRate();
                winnerId = vr.getVariantId();
            }
        }
        
        return TestResult.builder()
                .testId(testId)
                .testName(test.getName())
                .status(test.getStatus())
                .startTime(test.getStartTime())
                .endTime(test.getEndTime())
                .variants(variantResults)
                .winnerVariantId(winnerId)
                .significance(significance)
                .recommendation(determineRecommendation(significance, variantResults))
                .build();
    }

    /**
     * 获取测试详情
     */
    public ABTest getTest(String testId) {
        return activeTests.get(testId);
    }

    /**
     * 获取所有测试
     */
    public List<ABTest> getAllTests() {
        return new ArrayList<>(activeTests.values());
    }

    /**
     * 获取运行中的测试
     */
    public List<ABTest> getRunningTests() {
        return activeTests.values().stream()
                .filter(t -> t.getStatus() == TestStatus.RUNNING)
                .toList();
    }

    /**
     * 删除测试
     */
    public void deleteTest(String testId) {
        activeTests.remove(testId);
        testVariants.remove(testId);
        variantMetrics.remove(testId);
        
        // 清理用户分配
        userAssignments.entrySet().removeIf(e -> e.getKey().startsWith(testId + ":"));
        
        log.info("Deleted A/B test: {}", testId);
    }

    // ==================== 私有方法 ====================

    private void determineWinner(String testId) {
        TestResult result = getTestResult(testId);
        
        ABTest test = activeTests.get(testId);
        if (result.getSignificance() >= test.getMinSignificance() && result.getWinnerVariantId() != null) {
            test.setWinnerVariantId(result.getWinnerVariantId());
            test.setStatus(TestStatus.COMPLETED);
            log.info("A/B test {} winner determined: {} (significance: {})", 
                    testId, result.getWinnerVariantId(), result.getSignificance());
        }
    }

    private VariantResult calculateVariantResult(ABVariant variant, VariantMetrics metrics) {
        if (metrics == null) {
            return VariantResult.builder()
                    .variantId(variant.getVariantId())
                    .variantName(variant.getVariantName())
                    .isControl(variant.isControl())
                    .impressions(0)
                    .conversions(0)
                    .conversionRate(0.0)
                    .confidenceInterval(0.0)
                    .build();
        }
        
        double conversionRate = metrics.getImpressions() > 0 
                ? (double) metrics.getConversions() / metrics.getImpressions() 
                : 0;
        
        double confidenceInterval = calculateConfidenceInterval(
                conversionRate, metrics.getImpressions());
        
        return VariantResult.builder()
                .variantId(variant.getVariantId())
                .variantName(variant.getVariantName())
                .isControl(variant.isControl())
                .impressions((int) metrics.getImpressions())
                .conversions((int) metrics.getConversions())
                .conversionRate(conversionRate)
                .averageLatencyMs(metrics.getAverageLatency())
                .errorRate(metrics.getErrorRate())
                .revenue(metrics.getTotalRevenue())
                .confidenceInterval(confidenceInterval)
                .build();
    }

    private double calculateConfidenceInterval(double rate, long sampleSize) {
        if (sampleSize < 30) return 0;
        // 简化实现：95%置信区间
        double z = 1.96; // 95%
        double margin = z * Math.sqrt(rate * (1 - rate) / sampleSize);
        return margin * 100; // 返回百分比
    }

    private double calculateSignificance(List<VariantResult> results) {
        if (results.size() < 2) return 0;
        
        VariantResult control = results.stream()
                .filter(VariantResult::isControl)
                .findFirst()
                .orElse(null);
        
        if (control == null || control.getImpressions() < 100) return 0;
        
        double maxLift = 0;
        for (VariantResult variant : results) {
            if (variant.isControl()) continue;
            
            if (control.getConversionRate() > 0) {
                double lift = (variant.getConversionRate() - control.getConversionRate()) 
                        / control.getConversionRate();
                maxLift = Math.max(maxLift, Math.abs(lift));
            }
        }
        
        return Math.min(1.0, maxLift);
    }

    private String determineRecommendation(double significance, List<VariantResult> results) {
        if (significance < 0.8) {
            return "需要更多样本来得出结论";
        }
        
        VariantResult control = results.stream()
                .filter(VariantResult::isControl)
                .findFirst()
                .orElse(null);
        
        for (VariantResult variant : results) {
            if (variant.isControl()) continue;
            
            if (variant.getConversionRate() > (control != null ? control.getConversionRate() : 0)) {
                return "建议采用变体 " + variant.getVariantName() + "，预期提升 " 
                        + String.format("%.1f%%", (variant.getConversionRate() / 
                        (control != null && control.getConversionRate() > 0 ? control.getConversionRate() : 1) - 1) * 100);
            }
        }
        
        return "控制组表现最佳，无需变更";
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class ABTest {
        private String testId;
        private String name;
        private String description;
        private TestStatus status;
        private int variantCount;
        private double[] variantWeights;
        private double minSignificance;
        private int minSampleSize;
        private long createdTime;
        private long startTime;
        private long endTime;
        private String winnerVariantId;
    }

    @Data
    @Builder
    public static class ABVariant {
        private String variantId;
        private String variantName;
        private double weight;
        private boolean isControl;
        private String config;
    }

    @Data
    @Builder
    public static class UserAssignment {
        private String testId;
        private String userId;
        private String variantId;
        private long assignedTime;
    }

    @Data
    @Builder
    public static class TestResult {
        private String testId;
        private String testName;
        private TestStatus status;
        private long startTime;
        private long endTime;
        private List<VariantResult> variants;
        private String winnerVariantId;
        private double significance;
        private String recommendation;
    }

    @Data
    @Builder
    public static class VariantResult {
        private String variantId;
        private String variantName;
        private boolean isControl;
        private int impressions;
        private int conversions;
        private double conversionRate;
        private double averageLatencyMs;
        private double errorRate;
        private double revenue;
        private double confidenceInterval;
    }

    @Data
    @Builder
    public static class VariantMetrics {
        private String variantId;
        private long impressions;
        private long conversions;
        private double totalRevenue;
        private double totalLatency;
        private long latencyCount;
        private long errors;

        public void incrementImpressions() { impressions++; }
        public void incrementConversions() { conversions++; }
        public void addRevenue(double revenue) { totalRevenue += revenue; }
        public void recordLatency(double latency) { 
            totalLatency += latency; 
            latencyCount++; 
        }
        public void incrementErrors() { errors++; }

        public double getAverageLatency() {
            return latencyCount > 0 ? totalLatency / latencyCount : 0;
        }

        public double getErrorRate() {
            return impressions > 0 ? (double) errors / impressions : 0;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }
    }

    public enum TestStatus {
        DRAFT,
        RUNNING,
        PAUSED,
        COMPLETED,
        CANCELLED
    }

    public enum MetricType {
        IMPRESSION,
        CONVERSION,
        REVENUE,
        LATENCY,
        ERROR
    }
}