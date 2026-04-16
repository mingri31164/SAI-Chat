/*
 * Prompt调优器
 * 
 * 自动化优化Agent的Prompt模板
 */

package com.sai.chat.agent.rag.core.observe.prompt;

import com.sai.chat.agent.rag.core.observe.eval.EvalCase;
import com.sai.chat.agent.rag.core.observe.eval.EvalResult;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Prompt调优器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptTuner {

    private final Map<String, PromptVersion> promptVersions = new ConcurrentHashMap<>();
    private final Map<String, List<TuningIteration>> tuningHistory = new ConcurrentHashMap<>();

    private static final double MIN_IMPROVEMENT = 2.0;
    private static final int MAX_ITERATIONS = 20;

    /**
     * 创建Prompt版本
     */
    public PromptVersion createVersion(String promptId, String content, String description) {
        String versionId = promptId + "-v" + System.currentTimeMillis();
        
        PromptVersion version = PromptVersion.builder()
                .versionId(versionId)
                .promptId(promptId)
                .content(content)
                .description(description)
                .versionNumber(1)
                .status(VersionStatus.DRAFT)
                .createdTime(System.currentTimeMillis())
                .metrics(PromptMetrics.builder()
                        .evalScore(0.0)
                        .responseQuality(0.0)
                        .tokenCost(0)
                        .iterationCount(0)
                        .successRate(0.0)
                        .build())
                .build();
        
        promptVersions.put(versionId, version);
        tuningHistory.put(versionId, new ArrayList<>());
        
        log.info("Created prompt version: {} for prompt: {}", versionId, promptId);
        
        return version;
    }

    /**
     * 优化Prompt
     */
    public TuningResult optimize(String promptId, String currentContent, 
                                  List<EvalCase> testCases, TuningStrategy strategy) {
        // 获取或创建版本
        String versionId = promptId + "-current";
        PromptVersion version = promptVersions.get(versionId);
        
        if (version == null) {
            version = createVersion(promptId, currentContent, "Initial version");
            version.setVersionId(versionId);
        }
        
        TuningResult result = TuningResult.builder()
                .promptId(promptId)
                .initialContent(currentContent)
                .build();
        
        double bestScore = evaluatePrompt(currentContent, testCases);
        String bestContent = currentContent;
        
        result.setInitialScore(bestScore);
        result.setStartTime(System.currentTimeMillis());
        
        List<TuningIteration> iterations = tuningHistory.computeIfAbsent(versionId, k -> new ArrayList<>());
        
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // 生成变体
            List<PromptVariant> variants = generateVariants(currentContent, strategy, 3);
            
            double iterationBestScore = bestScore;
            String iterationBestContent = currentContent;
            PromptVariant bestVariant = null;
            
            for (PromptVariant variant : variants) {
                double score = evaluateVariant(variant, testCases);
                variant.setScore(score);
                
                if (score > iterationBestScore) {
                    iterationBestScore = score;
                    iterationBestContent = variant.getContent();
                    bestVariant = variant;
                }
            }
            
            TuningIteration iteration = TuningIteration.builder()
                    .iterationNumber(i + 1)
                    .variants(variants)
                    .bestVariant(bestVariant)
                    .bestScore(iterationBestScore)
                    .improvement(iterationBestScore - bestScore)
                    .build();
            
            iterations.add(iteration);
            
            if (iterationBestScore > bestScore + MIN_IMPROVEMENT) {
                bestScore = iterationBestScore;
                bestContent = iterationBestContent;
                currentContent = bestContent;
                
                log.info("Iteration {}: improved from {} to {} (+{})", 
                        i + 1, bestScore - iterationBestScore + bestScore, bestScore, iteration.getImprovement());
            } else {
                log.info("Iteration {}: no significant improvement (best: {}, current: {})", 
                        i + 1, bestScore, iterationBestScore);
                break;
            }
        }
        
        result.setFinalContent(bestContent);
        result.setFinalScore(bestScore);
        result.setTotalImprovement(bestScore - result.getInitialScore());
        result.setIterations(iterations.size());
        result.setEndTime(System.currentTimeMillis());
        result.setSuccessful(result.getTotalImprovement() >= MIN_IMPROVEMENT);
        
        // 更新版本
        version.setContent(bestContent);
        version.getMetrics().setEvalScore(bestScore);
        
        return result;
    }

    /**
     * 生成Prompt变体
     */
    public List<PromptVariant> generateVariants(String content, TuningStrategy strategy, int count) {
        List<PromptVariant> variants = new ArrayList<>();
        
        switch (strategy) {
            case GRAMMATICAL -> {
                variants.addAll(generateGrammaticalVariants(content, count));
            }
            case STRUCTURAL -> {
                variants.addAll(generateStructuralVariants(content, count));
            }
            case CONCISENESS -> {
                variants.addAll(generateConciseVariants(content, count));
            }
            case EXAMPLES -> {
                variants.addAll(generateExampleVariants(content, count));
            }
            case HYBRID -> {
                variants.addAll(generateHybridVariants(content, count));
            }
        }
        
        return variants;
    }

    /**
     * 批量测试Prompt变体
     */
    public List<VariantResult> batchTest(String promptId, List<PromptVariant> variants, 
                                          List<EvalCase> testCases) {
        return variants.stream()
                .map(variant -> {
                    double score = evaluateVariant(variant, testCases);
                    variant.setScore(score);
                    
                    return VariantResult.builder()
                            .variantId(variant.getVariantId())
                            .content(variant.getContent())
                            .score(score)
                            .responseQuality(calculateResponseQuality(variant))
                            .tokenCost(estimateTokenCost(variant.getContent()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取最佳Prompt
     */
    public PromptVersion getBestVersion(String promptId) {
        return promptVersions.values().stream()
                .filter(v -> v.getPromptId().equals(promptId))
                .max(Comparator.comparingDouble(v -> v.getMetrics().getEvalScore()))
                .orElse(null);
    }

    /**
     * 获取版本历史
     */
    public List<PromptVersion> getVersionHistory(String promptId) {
        return promptVersions.values().stream()
                .filter(v -> v.getPromptId().equals(promptId))
                .sorted(Comparator.comparingInt(PromptVersion::getVersionNumber))
                .collect(Collectors.toList());
    }

    /**
     * 获取调优历史
     */
    public List<TuningIteration> getTuningHistory(String versionId) {
        return tuningHistory.getOrDefault(versionId, new ArrayList<>());
    }

    /**
     * 回滚到指定版本
     */
    public PromptVersion rollback(String versionId) {
        PromptVersion version = promptVersions.get(versionId);
        if (version != null) {
            version.setStatus(VersionStatus.ROLLED_BACK);
            log.info("Rolled back prompt version: {}", versionId);
        }
        return version;
    }

    /**
     * 发布版本
     */
    public PromptVersion publish(String versionId) {
        PromptVersion version = promptVersions.get(versionId);
        if (version != null) {
            version.setStatus(VersionStatus.PUBLISHED);
            version.setPublishedTime(System.currentTimeMillis());
            log.info("Published prompt version: {}", versionId);
        }
        return version;
    }

    // ==================== 私有方法 ====================

    private double evaluatePrompt(String content, List<EvalCase> testCases) {
        // 简化实现：基于启发式规则评估
        double score = 50.0;
        
        // 检查基本结构
        if (content.contains("请") || content.contains("你是一个") || content.contains("你是一个")) {
            score += 10;
        }
        
        // 检查约束
        if (content.contains("必须") || content.contains("不要") || content.contains("确保")) {
            score += 10;
        }
        
        // 检查示例
        if (content.contains("例如") || content.contains("示例") || content.contains("比如")) {
            score += 10;
        }
        
        // 检查格式
        if (content.contains("\n") && content.length() > 100) {
            score += 5;
        }
        
        // 长度惩罚
        if (content.length() > 2000) {
            score -= 10;
        } else if (content.length() < 100) {
            score -= 5;
        }
        
        return Math.max(0, Math.min(100, score));
    }

    private double evaluateVariant(PromptVariant variant, List<EvalCase> testCases) {
        return evaluatePrompt(variant.getContent(), testCases);
    }

    private List<PromptVariant> generateGrammaticalVariants(String content, int count) {
        List<PromptVariant> variants = new ArrayList<>();
        
        // 简化：生成3个语法变体
        variants.add(PromptVariant.builder()
                .variantId("gram-1")
                .content(content.replace("你是一个", "你是"))
                .changeType("grammatical")
                .build());
        
        variants.add(PromptVariant.builder()
                .variantId("gram-2")
                .content(content.replace("请", "请务必"))
                .changeType("grammatical")
                .build());
        
        variants.add(PromptVariant.builder()
                .variantId("gram-3")
                .content(content.replace("不要", "禁止"))
                .changeType("grammatical")
                .build());
        
        return variants.subList(0, Math.min(count, variants.size()));
    }

    private List<PromptVariant> generateStructuralVariants(String content, int count) {
        List<PromptVariant> variants = new ArrayList<>();
        
        // 添加结构化标记
        variants.add(PromptVariant.builder()
                .variantId("struct-1")
                .content("## 角色\n" + content + "\n## 要求")
                .changeType("structural")
                .build());
        
        // 使用列表格式
        variants.add(PromptVariant.builder()
                .variantId("struct-2")
                .content(content.replace("。", "。\n- "))
                .changeType("structural")
                .build());
        
        return variants.subList(0, Math.min(count, variants.size()));
    }

    private List<PromptVariant> generateConciseVariants(String content, int count) {
        List<PromptVariant> variants = new ArrayList<>();
        
        // 精简版本
        String concise = content.replaceAll("\\s+", " ").trim();
        variants.add(PromptVariant.builder()
                .variantId("concise-1")
                .content(concise)
                .changeType("conciseness")
                .build());
        
        // 更精简
        String[] sentences = concise.split("[。!?]");
        if (sentences.length > 3) {
            String shortVersion = sentences[0] + "。" + sentences[1] + "。" + sentences[2] + "。";
            variants.add(PromptVariant.builder()
                    .variantId("concise-2")
                    .content(shortVersion)
                    .changeType("conciseness")
                    .build());
        }
        
        return variants.subList(0, Math.min(count, variants.size()));
    }

    private List<PromptVariant> generateExampleVariants(String content, int count) {
        List<PromptVariant> variants = new ArrayList<>();
        
        // 添加示例
        variants.add(PromptVariant.builder()
                .variantId("example-1")
                .content(content + "\n\n示例：\n输入：[问题]\n输出：[回答]")
                .changeType("examples")
                .build());
        
        // 添加详细示例
        variants.add(PromptVariant.builder()
                .variantId("example-2")
                .content(content + "\n\n示例1：\n示例2：")
                .changeType("examples")
                .build());
        
        return variants.subList(0, Math.min(count, variants.size()));
    }

    private List<PromptVariant> generateHybridVariants(String content, int count) {
        List<PromptVariant> variants = new ArrayList<>();
        
        // 综合变体
        String hybrid = "## 任务\n" + content.replace("你是一个", "你是") 
                + "\n\n## 约束\n- 简洁\n- 准确";
        
        variants.add(PromptVariant.builder()
                .variantId("hybrid-1")
                .content(hybrid)
                .changeType("hybrid")
                .build());
        
        return variants.subList(0, Math.min(count, variants.size()));
    }

    private double calculateResponseQuality(PromptVariant variant) {
        // 简化实现
        return variant.getScore() * 0.8 + Math.random() * 20;
    }

    private int estimateTokenCost(String content) {
        // 粗略估计：中文每个字符约1.5个token，英文每个词约1.3个token
        return (int) (content.length() * 1.2);
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class PromptVersion {
        private String versionId;
        private String promptId;
        private String content;
        private String description;
        private int versionNumber;
        private VersionStatus status;
        private long createdTime;
        private long publishedTime;
        private PromptMetrics metrics;
    }

    @Data
    @Builder
    public static class PromptVariant {
        private String variantId;
        private String content;
        private String changeType;
        private double score;
    }

    @Data
    @Builder
    public static class PromptMetrics {
        private double evalScore;
        private double responseQuality;
        private int tokenCost;
        private int iterationCount;
        private double successRate;
    }

    @Data
    @Builder
    public static class TuningIteration {
        private int iterationNumber;
        private List<PromptVariant> variants;
        private PromptVariant bestVariant;
        private double bestScore;
        private double improvement;
    }

    @Data
    @Builder
    public static class TuningResult {
        private String promptId;
        private String initialContent;
        private double initialScore;
        private String finalContent;
        private double finalScore;
        private double totalImprovement;
        private int iterations;
        private boolean successful;
        private long startTime;
        private long endTime;
    }

    @Data
    @Builder
    public static class VariantResult {
        private String variantId;
        private String content;
        private double score;
        private double responseQuality;
        private int tokenCost;
    }

    public enum TuningStrategy {
        GRAMMATICAL,
        STRUCTURAL,
        CONCISENESS,
        EXAMPLES,
        HYBRID
    }

    public enum VersionStatus {
        DRAFT,
        TESTING,
        PUBLISHED,
        ROLLED_BACK,
        ARCHIVED
    }
}