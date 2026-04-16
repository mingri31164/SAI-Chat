/*
 * Prompt调优控制器
 */

package com.sai.chat.agent.rag.core.observe.prompt;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.rag.core.observe.eval.EvalCase;
import com.sai.chat.agent.rag.core.observe.prompt.PromptTuner.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Prompt调优控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt")
@RequiredArgsConstructor
public class PromptController {

    private final PromptTuner promptTuner;

    /**
     * 创建Prompt版本
     */
    @PostMapping("/version")
    public Result<PromptVersion> createVersion(
            @RequestParam String promptId,
            @RequestParam String content,
            @RequestParam(required = false) String description) {
        
        PromptVersion version = promptTuner.createVersion(promptId, content, description);
        
        return new Result<PromptVersion>()
                .setCode(Result.SUCCESS_CODE)
                .setData(version);
    }

    /**
     * 优化Prompt
     */
    @PostMapping("/optimize")
    public Result<TuningResult> optimize(
            @RequestParam String promptId,
            @RequestParam String content,
            @RequestBody List<EvalCase> testCases,
            @RequestParam(defaultValue = "HYBRID") TuningStrategy strategy) {
        
        TuningResult result = promptTuner.optimize(promptId, content, testCases, strategy);
        
        return new Result<TuningResult>()
                .setCode(Result.SUCCESS_CODE)
                .setData(result);
    }

    /**
     * 生成变体
     */
    @PostMapping("/variants")
    public Result<List<PromptVariant>> generateVariants(
            @RequestParam String content,
            @RequestParam(defaultValue = "HYBRID") TuningStrategy strategy,
            @RequestParam(defaultValue = "3") int count) {
        
        List<PromptVariant> variants = promptTuner.generateVariants(content, strategy, count);
        
        return new Result<List<PromptVariant>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(variants);
    }

    /**
     * 批量测试变体
     */
    @PostMapping("/batch-test")
    public Result<List<VariantResult>> batchTest(
            @RequestParam String promptId,
            @RequestBody List<PromptVariant> variants,
            @RequestBody List<EvalCase> testCases) {
        
        List<VariantResult> results = promptTuner.batchTest(promptId, variants, testCases);
        
        return new Result<List<VariantResult>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(results);
    }

    /**
     * 获取最佳版本
     */
    @GetMapping("/best/{promptId}")
    public Result<PromptVersion> getBestVersion(@PathVariable String promptId) {
        PromptVersion version = promptTuner.getBestVersion(promptId);
        
        return new Result<PromptVersion>()
                .setCode(Result.SUCCESS_CODE)
                .setData(version);
    }

    /**
     * 获取版本历史
     */
    @GetMapping("/history/{promptId}")
    public Result<List<PromptVersion>> getVersionHistory(@PathVariable String promptId) {
        List<PromptVersion> versions = promptTuner.getVersionHistory(promptId);
        
        return new Result<List<PromptVersion>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(versions);
    }

    /**
     * 获取调优历史
     */
    @GetMapping("/tuning-history/{versionId}")
    public Result<List<TuningIteration>> getTuningHistory(@PathVariable String versionId) {
        List<TuningIteration> history = promptTuner.getTuningHistory(versionId);
        
        return new Result<List<TuningIteration>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(history);
    }

    /**
     * 回滚版本
     */
    @PostMapping("/rollback/{versionId}")
    public Result<PromptVersion> rollback(@PathVariable String versionId) {
        PromptVersion version = promptTuner.rollback(versionId);
        
        return new Result<PromptVersion>()
                .setCode(Result.SUCCESS_CODE)
                .setData(version);
    }

    /**
     * 发布版本
     */
    @PostMapping("/publish/{versionId}")
    public Result<PromptVersion> publish(@PathVariable String versionId) {
        PromptVersion version = promptTuner.publish(versionId);
        
        return new Result<PromptVersion>()
                .setCode(Result.SUCCESS_CODE)
                .setData(version);
    }
}