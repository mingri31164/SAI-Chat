/*
 * A/B测试控制器
 */

package com.sai.chat.agent.rag.core.observe.abtest;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.rag.core.observe.abtest.ABTestManager.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A/B测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/abtest")
@RequiredArgsConstructor
public class ABTestController {

    private final ABTestManager abTestManager;

    /**
     * 创建A/B测试
     */
    @PostMapping("/test")
    public Result<ABTest> createTest(@RequestBody ABTestRequest request) {
        ABTest test = ABTest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .variantCount(request.getVariantCount())
                .variantWeights(request.getVariantWeights())
                .minSignificance(request.getMinSignificance())
                .minSampleSize(request.getMinSampleSize())
                .build();
        
        test = abTestManager.createTest(test);
        
        return new Result<ABTest>()
                .setCode(Result.SUCCESS_CODE)
                .setData(test);
    }

    /**
     * 启动测试
     */
    @PostMapping("/test/{testId}/start")
    public Result<ABTest> startTest(@PathVariable String testId) {
        ABTest test = abTestManager.startTest(testId);
        
        return new Result<ABTest>()
                .setCode(Result.SUCCESS_CODE)
                .setData(test);
    }

    /**
     * 暂停测试
     */
    @PostMapping("/test/{testId}/pause")
    public Result<ABTest> pauseTest(@PathVariable String testId) {
        ABTest test = abTestManager.pauseTest(testId);
        
        return new Result<ABTest>()
                .setCode(Result.SUCCESS_CODE)
                .setData(test);
    }

    /**
     * 结束测试
     */
    @PostMapping("/test/{testId}/end")
    public Result<ABTest> endTest(
            @PathVariable String testId,
            @RequestParam(defaultValue = "true") boolean markWinner) {
        
        ABTest test = abTestManager.endTest(testId, markWinner);
        
        return new Result<ABTest>()
                .setCode(Result.SUCCESS_CODE)
                .setData(test);
    }

    /**
     * 获取测试结果
     */
    @GetMapping("/test/{testId}/result")
    public Result<TestResult> getTestResult(@PathVariable String testId) {
        TestResult result = abTestManager.getTestResult(testId);
        
        return new Result<TestResult>()
                .setCode(Result.SUCCESS_CODE)
                .setData(result);
    }

    /**
     * 获取测试详情
     */
    @GetMapping("/test/{testId}")
    public Result<ABTest> getTest(@PathVariable String testId) {
        ABTest test = abTestManager.getTest(testId);
        
        return new Result<ABTest>()
                .setCode(Result.SUCCESS_CODE)
                .setData(test);
    }

    /**
     * 获取所有测试
     */
    @GetMapping("/tests")
    public Result<List<ABTest>> getAllTests() {
        List<ABTest> tests = abTestManager.getAllTests();
        
        return new Result<List<ABTest>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(tests);
    }

    /**
     * 获取运行中的测试
     */
    @GetMapping("/tests/running")
    public Result<List<ABTest>> getRunningTests() {
        List<ABTest> tests = abTestManager.getRunningTests();
        
        return new Result<List<ABTest>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(tests);
    }

    /**
     * 删除测试
     */
    @DeleteMapping("/test/{testId}")
    public Result<Void> deleteTest(@PathVariable String testId) {
        abTestManager.deleteTest(testId);
        
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE);
    }

    /**
     * 为用户分配变体
     */
    @GetMapping("/test/{testId}/assign")
    public Result<String> assignVariant(
            @PathVariable String testId,
            @RequestParam String userId) {
        
        String variantId = abTestManager.assignVariant(testId, userId);
        
        return new Result<String>()
                .setCode(Result.SUCCESS_CODE)
                .setData(variantId);
    }

    /**
     * 记录指标
     */
    @PostMapping("/test/{testId}/metric")
    public Result<Void> recordMetric(
            @PathVariable String testId,
            @RequestBody MetricRequest request) {
        
        abTestManager.recordMetric(
                testId, 
                request.getVariantId(), 
                request.getMetricType(), 
                request.getValue());
        
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE);
    }

    // ==================== 请求类 ====================

    @lombok.Data
    public static class ABTestRequest {
        private String name;
        private String description;
        private int variantCount;
        private double[] variantWeights;
        private double minSignificance = 0.95;
        private int minSampleSize = 1000;
    }

    @lombok.Data
    public static class MetricRequest {
        private String variantId;
        private MetricType metricType;
        private double value;
    }
}