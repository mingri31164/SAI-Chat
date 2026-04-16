/*
 * 复盘与模式控制器
 */

package com.sai.chat.agent.rag.core.observe.review;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.rag.core.observe.review.PatternRepository.*;
import com.sai.chat.agent.rag.core.observe.review.ReviewAnalyzer.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 复盘与模式控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewAnalyzer reviewAnalyzer;
    private final PatternRepository patternRepository;

    /**
     * 执行复盘
     */
    @PostMapping("/analyze")
    public Result<ReviewReport> analyzeReview(
            @RequestParam String traceId,
            @RequestParam(required = false) String sessionId) {
        
        // 简化实现：创建空复盘报告
        ReviewReport report = ReviewReport.builder()
                .reviewId("review-" + traceId)
                .traceId(traceId)
                .sessionId(sessionId)
                .timestamp(System.currentTimeMillis())
                .success(true)
                .keyDecisions(List.of())
                .successFactors(List.of())
                .failureReasons(List.of())
                .improvements(List.of())
                .confidence(0.8)
                .lessonType(LessonType.SUCCESS_PATTERN)
                .build();
        
        return new Result<ReviewReport>()
                .setCode(Result.SUCCESS_CODE)
                .setData(report);
    }

    /**
     * 批量复盘
     */
    @PostMapping("/batch")
    public Result<List<ReviewReport>> batchReview(@RequestBody List<String> traceIds) {
        List<ReviewReport> reports = traceIds.stream()
                .map(traceId -> ReviewReport.builder()
                        .reviewId("batch-" + traceId)
                        .traceId(traceId)
                        .timestamp(System.currentTimeMillis())
                        .success(true)
                        .lessonType(LessonType.SUCCESS_PATTERN)
                        .build())
                .toList();
        
        return new Result<List<ReviewReport>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(reports);
    }

    /**
     * 聚合复盘结果
     */
    @PostMapping("/aggregate")
    public Result<AggregatedReview> aggregateReviews(@RequestBody List<ReviewReport> reports) {
        AggregatedReview aggregated = reviewAnalyzer.aggregateReviews(reports);
        
        return new Result<AggregatedReview>()
                .setCode(Result.SUCCESS_CODE)
                .setData(aggregated);
    }

    /**
     * 添加成功模式
     */
    @PostMapping("/pattern")
    public Result<SuccessPattern> addPattern(@RequestBody SuccessPattern pattern) {
        String patternId = "pattern-" + System.currentTimeMillis();
        pattern.setPatternId(patternId);
        SuccessPattern saved = patternRepository.addPattern(pattern);
        
        return new Result<SuccessPattern>()
                .setCode(Result.SUCCESS_CODE)
                .setData(saved);
    }

    /**
     * 获取模式
     */
    @GetMapping("/pattern/{patternId}")
    public Result<SuccessPattern> getPattern(@PathVariable String patternId) {
        SuccessPattern pattern = patternRepository.getPattern(patternId);
        
        return new Result<SuccessPattern>()
                .setCode(Result.SUCCESS_CODE)
                .setData(pattern);
    }

    /**
     * 搜索模式
     */
    @GetMapping("/patterns/search")
    public Result<List<SuccessPattern>> searchPatterns(@RequestParam String query) {
        List<SuccessPattern> patterns = patternRepository.searchPatterns(query);
        
        return new Result<List<SuccessPattern>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(patterns);
    }

    /**
     * 获取所有模式
     */
    @GetMapping("/patterns")
    public Result<List<SuccessPattern>> getAllPatterns() {
        List<SuccessPattern> patterns = patternRepository.getAllPatterns();
        
        return new Result<List<SuccessPattern>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(patterns);
    }

    /**
     * 获取热门模式
     */
    @GetMapping("/patterns/top")
    public Result<List<SuccessPattern>> getTopPatterns(@RequestParam(defaultValue = "10") int limit) {
        List<SuccessPattern> patterns = patternRepository.getTopPatterns(limit);
        
        return new Result<List<SuccessPattern>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(patterns);
    }

    /**
     * 添加失败教训
     */
    @PostMapping("/lesson")
    public Result<FailureLesson> addLesson(@RequestBody FailureLesson lesson) {
        String lessonId = "lesson-" + System.currentTimeMillis();
        lesson.setLessonId(lessonId);
        FailureLesson saved = patternRepository.addLesson(lesson);
        
        return new Result<FailureLesson>()
                .setCode(Result.SUCCESS_CODE)
                .setData(saved);
    }

    /**
     * 获取教训
     */
    @GetMapping("/lesson/{lessonId}")
    public Result<FailureLesson> getLesson(@PathVariable String lessonId) {
        FailureLesson lesson = patternRepository.getLesson(lessonId);
        
        return new Result<FailureLesson>()
                .setCode(Result.SUCCESS_CODE)
                .setData(lesson);
    }

    /**
     * 搜索教训
     */
    @GetMapping("/lessons/search")
    public Result<List<FailureLesson>> searchLessons(@RequestParam String query) {
        List<FailureLesson> lessons = patternRepository.searchLessons(query);
        
        return new Result<List<FailureLesson>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(lessons);
    }

    /**
     * 获取所有教训
     */
    @GetMapping("/lessons")
    public Result<List<FailureLesson>> getAllLessons() {
        List<FailureLesson> lessons = patternRepository.getAllLessons();
        
        return new Result<List<FailureLesson>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(lessons);
    }

    /**
     * 获取相关模式
     */
    @GetMapping("/patterns/related")
    public Result<List<SuccessPattern>> getRelatedPatterns(@RequestParam String context) {
        List<SuccessPattern> patterns = patternRepository.getRelatedPatterns(context);
        
        return new Result<List<SuccessPattern>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(patterns);
    }

    /**
     * 更新模式评分
     */
    @PutMapping("/pattern/{patternId}/score")
    public Result<Void> updatePatternScore(
            @PathVariable String patternId,
            @RequestParam double delta) {
        
        patternRepository.updatePatternScore(patternId, delta);
        
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE);
    }

    /**
     * 删除模式
     */
    @DeleteMapping("/pattern/{patternId}")
    public Result<Void> deletePattern(@PathVariable String patternId) {
        patternRepository.removePattern(patternId);
        
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE);
    }

    /**
     * 删除教训
     */
    @DeleteMapping("/lesson/{lessonId}")
    public Result<Void> deleteLesson(@PathVariable String lessonId) {
        patternRepository.removeLesson(lessonId);
        
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE);
    }

    /**
     * 导出模式
     */
    @GetMapping("/export")
    public Result<Map<String, Object>> exportPatterns() {
        Map<String, Object> exported = patternRepository.exportPatterns();
        
        return new Result<Map<String, Object>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(exported);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = patternRepository.getStatistics();
        
        return new Result<Map<String, Object>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(stats);
    }
}