/*
 * 评测运行器
 */

package com.sai.chat.agent.rag.core.observe.eval;

import com.sai.chat.agent.rag.core.agent.AgentService;
import com.sai.chat.agent.rag.core.agent.request.AgentRequest;
import com.sai.chat.agent.rag.core.agent.response.AgentResponse;
import com.sai.chat.agent.rag.core.agent.state.AgentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * 评测运行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EvalRunner {

    private final AgentService agentService;
    private final List<Evaluator> evaluators;
    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    private static final double PASS_THRESHOLD = 70.0;

    public EvalReport runEvaluation(List<EvalCase> testCases) {
        return runEvaluation(testCases, false);
    }

    public EvalReport runEvaluation(List<EvalCase> testCases, boolean parallel) {
        String reportId = "eval-" + UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        log.info("开始评测: reportId={}, cases={}", reportId, testCases.size());

        List<EvalResult> results;
        if (parallel) {
            results = runParallel(testCases);
        } else {
            results = runSequential(testCases);
        }

        long endTime = System.currentTimeMillis();
        EvalReport report = EvalReport.fromResults(reportId, results, startTime, endTime);

        log.info("评测完成: reportId={}, passed={}/{}, score={:.2f}%",
                reportId, report.getPassedCases(), report.getTotalCases(), report.getPassRate() * 100);

        return report;
    }

    private List<EvalResult> runSequential(List<EvalCase> testCases) {
        List<EvalResult> results = new ArrayList<>();
        for (EvalCase testCase : testCases) {
            try {
                EvalResult result = executeCase(testCase);
                results.add(result);
            } catch (Exception e) {
                log.error("用例执行失败: caseId={}", testCase.getId(), e);
                results.add(createErrorResult(testCase, e.getMessage()));
            }
        }
        return results;
    }

    private List<EvalResult> runParallel(List<EvalCase> testCases) {
        List<Future<EvalResult>> futures = new ArrayList<>();
        for (EvalCase testCase : testCases) {
            Future<EvalResult> future = executor.submit(() -> {
                try {
                    return executeCase(testCase);
                } catch (Exception e) {
                    log.error("用例执行失败: caseId={}", testCase.getId(), e);
                    return createErrorResult(testCase, e.getMessage());
                }
            });
            futures.add(future);
        }

        List<EvalResult> results = new ArrayList<>();
        for (Future<EvalResult> future : futures) {
            try {
                results.add(future.get(5, TimeUnit.MINUTES));
            } catch (Exception e) {
                log.error("获取评测结果失败", e);
            }
        }
        return results;
    }

    private EvalResult executeCase(EvalCase testCase) {
        long startTime = System.currentTimeMillis();
        log.info("执行评测用例: caseId={}, name={}", testCase.getId(), testCase.getName());

        AgentRequest request = AgentRequest.builder()
                .sessionId("eval-" + testCase.getId())
                .userId("eval-user")
                .question(testCase.getInput())
                .maxIterations(testCase.getMaxIterations() != null ? testCase.getMaxIterations() : 10)
                .build();

        AgentResponse response = agentService.execute(request);
        long duration = System.currentTimeMillis() - startTime;
        String actualAnswer = response.getAnswer() != null ? response.getAnswer() : "";

        List<String> toolCallNames = response.getToolCallTrace() != null
                ? response.getToolCallTrace().stream().map(r -> r.getToolName()).toList()
                : List.of();

        EvalResult result = EvalResult.builder()
                .caseId(testCase.getId())
                .caseName(testCase.getName())
                .actualAnswer(actualAnswer)
                .durationMs(duration)
                .iterations(response.getTotalIterations())
                .toolCalls(toolCallNames)
                .tags(testCase.getTags())
                .build();

        Map<String, Double> dimensionScores = new HashMap<>();
        for (Evaluator evaluator : evaluators) {
            try {
                Evaluator.Evaluation evaluation = evaluator.evaluate(testCase, result, actualAnswer);
                dimensionScores.put(evaluator.getDimension(), evaluation.score());
                result.addNote(evaluator.getName() + ": " + evaluation.details());

                if (!evaluation.matchedKeywords().isEmpty() || !evaluation.unmatchedKeywords().isEmpty()) {
                    result.setMatchDetails(EvalResult.MatchDetails.builder()
                            .keywordMatched(evaluation.matchedKeywords().size())
                            .keywordTotal(evaluation.matchedKeywords().size() + evaluation.unmatchedKeywords().size())
                            .keywordCoverage(evaluation.score() / 100.0)
                            .patternMatched(evaluation.score() == 100)
                            .build());
                }
            } catch (Exception e) {
                log.warn("评估器执行失败: {}", evaluator.getName(), e);
                dimensionScores.put(evaluator.getDimension(), 0.0);
                result.addNote(evaluator.getName() + ": 评估异常 - " + e.getMessage());
            }
        }

        result.setDimensionScores(dimensionScores);

        double overallScore = dimensionScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        result.setScore(overallScore);

        boolean passed = overallScore >= PASS_THRESHOLD
                && response.getStatus() != AgentStatus.FAILED
                && response.getStatus() != AgentStatus.EXCEEDED;
        result.setPassed(passed);

        if (!passed) {
            if (overallScore < PASS_THRESHOLD) {
                result.setFailureReason("综合得分低于阈值: " + String.format("%.2f < %.2f", overallScore, PASS_THRESHOLD));
            } else if (response.getStatus() == AgentStatus.FAILED) {
                result.setFailureReason("执行出错: " + response.getErrorMessage());
            } else if (response.getStatus() == AgentStatus.EXCEEDED) {
                result.setFailureReason("达到最大迭代次数");
            }
        }

        log.info("用例执行完成: caseId={}, passed={}, score={:.2f}%", testCase.getId(), passed, overallScore);
        return result;
    }

    private EvalResult createErrorResult(EvalCase testCase, String errorMessage) {
        return EvalResult.builder()
                .caseId(testCase.getId())
                .caseName(testCase.getName())
                .passed(false)
                .score(0)
                .errorMessage(errorMessage)
                .failureReason("执行异常: " + errorMessage)
                .tags(testCase.getTags())
                .build();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
