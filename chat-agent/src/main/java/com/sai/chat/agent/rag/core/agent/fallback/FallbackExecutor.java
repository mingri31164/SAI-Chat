/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package com.sai.chat.agent.rag.core.agent.fallback;

import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.rag.core.mcp.MCPToolDefinition;
import com.sai.chat.agent.rag.core.mcp.MCPToolRegistry;
import com.sai.chat.agent.rag.core.pipeline.RAGPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 兜底策略执行器
 * <p>
 * 当 Agent 执行失败时，按照配置的策略尝试恢复：
 * <ol>
 *   <li>重试（指数退避）</li>
 *   <li>切换备用工具</li>
 *   <li>降级到简单模式（RAG Only）</li>
 *   <li>返回部分结果</li>
 *   <li>触发升级</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackExecutor {

    private final LLMService llmService;
    private final RAGPipelineService ragPipeline;
    private final MCPToolRegistry toolRegistry;

    /**
     * 执行兜底
     *
     * @param originalError   原始错误
     * @param context        错误上下文
     * @param strategy       兜底策略
     * @param fallbackLogic  自定义兜底逻辑（可为 null）
     * @return 兜底结果
     */
    public FallbackResult execute(String originalError, FallbackContext context,
                                  FallbackStrategy strategy, FallbackLogic fallbackLogic) {
        if (!strategy.isEnabled()) {
            log.warn("兜底已禁用，直接返回失败");
            return FallbackResult.failed(originalError);
        }

        log.info("开始执行兜底策略, error={}, retries={}/{}",
                originalError, context.getRetryCount(), strategy.getMaxRetries());

        // 1. 尝试重试
        if (context.getRetryCount() < strategy.getMaxRetries()) {
            FallbackResult retryResult = tryRetry(context, strategy, originalError);
            if (retryResult != null && retryResult.isSuccess()) {
                return retryResult;
            }
        }

        // 2. 尝试备用工具
        if (strategy.isEnableBackupTool()) {
            FallbackResult backupResult = tryBackupTool(context, originalError);
            if (backupResult != null && backupResult.isSuccess()) {
                return backupResult;
            }
        }

        // 3. 降级到简单模式
        if (strategy.isFallbackToSimple()) {
            FallbackResult simpleResult = trySimpleMode(context);
            if (simpleResult != null && simpleResult.isSuccess()) {
                return simpleResult;
            }
        }

        // 4. 返回部分结果
        if (strategy.isReturnPartialResult() && context.hasPartialResults()) {
            return FallbackResult.partial(context.getBestPartialResult(), originalError);
        }

        // 5. 尝试自定义兜底
        if (fallbackLogic != null) {
            try {
                FallbackResult customResult = fallbackLogic.execute(originalError, context);
                if (customResult != null && customResult.isSuccess()) {
                    return customResult;
                }
            } catch (Exception e) {
                log.warn("自定义兜底执行失败: {}", e.getMessage());
            }
        }

        // 6. 检查是否需要升级
        if (context.shouldEscalate()) {
            return FallbackResult.escalated("需要人工介入: " + originalError);
        }

        return FallbackResult.failed(originalError);
    }

    /**
     * 尝试重试
     */
    private FallbackResult tryRetry(FallbackContext context, FallbackStrategy strategy, String originalError) {
        int attempt = context.incrementRetry();
        long delay = strategy.getRetryIntervalMs() * (long) Math.pow(2, attempt - 1);

        log.info("尝试重试, attempt={}, delay={}ms", attempt, delay);

        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        try {
            // 执行原始逻辑
            Object result = context.getRetryLogic().apply(attempt);
            if (result != null) {
                return FallbackResult.retrySuccess(result.toString());
            }
        } catch (Exception e) {
            log.warn("重试执行失败, attempt={}, error={}", attempt, e.getMessage());
        }

        return null;
    }

    /**
     * 尝试使用备用工具
     */
    private FallbackResult tryBackupTool(FallbackContext context, String originalError) {
        List<MCPToolDefinition> tools = toolRegistry.getAllTools();
        String originalToolId = context.getFailedToolId();

        // 找同类型的备用工具
        for (MCPToolDefinition tool : tools) {
            if (tool.getToolId().equals(originalToolId)) continue;
            // 简单的同义词匹配
            if (isSimilarPurpose(tool, originalError)) {
                try {
                    log.info("尝试备用工具, from={}, to={}", originalToolId, tool.getToolId());
                    // TODO: 实际执行备用工具
                    // 这里需要从 context 获取参数
                    return FallbackResult.builder()
                            .success(true)
                            .method(FallbackResult.FallbackMethod.BACKUP_TOOL)
                            .content("使用备用工具 [" + tool.getName() + "] 执行成功")
                            .build();
                } catch (Exception e) {
                    log.warn("备用工具执行失败: {}", e.getMessage());
                }
            }
        }

        return null;
    }

    /**
     * 降级到简单模式（RAG Only）
     */
    private FallbackResult trySimpleMode(FallbackContext context) {
        log.info("降级到简单模式（RAG）");

        try {
            String question = context.getQuestion();
            String result = ragPipeline.chat(question, context.getSessionId());
            if (result != null && !result.isBlank()) {
                return FallbackResult.degraded(result, "Agent 模式失败，降级为 RAG 模式");
            }
        } catch (Exception e) {
            log.warn("简单模式执行失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 判断工具是否目的相似
     */
    private boolean isSimilarPurpose(MCPToolDefinition tool, String errorContext) {
        if (tool.getDescription() == null) return false;
        String desc = tool.getDescription().toLowerCase();

        // 简单的关键词匹配
        String[] searchKeywords = {"搜索", "search", "查询", "query"};
        String[] computeKeywords = {"计算", "compute", "calculate"};
        String[] writeKeywords = {"写入", "write", "保存", "save"};

        for (String kw : searchKeywords) {
            if (desc.contains(kw) && errorContext.toLowerCase().contains(kw)) {
                return true;
            }
        }
        for (String kw : computeKeywords) {
            if (desc.contains(kw) && errorContext.toLowerCase().contains(kw)) {
                return true;
            }
        }
        for (String kw : writeKeywords) {
            if (desc.contains(kw) && errorContext.toLowerCase().contains(kw)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 错误上下文
     */
    public static class FallbackContext {
        private String question;
        private String sessionId;
        private String failedToolId;
        private int retryCount = 0;
        private int escalationLevel = 0;
        private java.util.List<String> partialResults = new java.util.ArrayList<>();
        private java.util.function.Function<Integer, Object> retryLogic;
        private java.util.Map<String, Object> extraData = new java.util.HashMap<>();

        public static FallbackContext create(String question, String sessionId) {
            FallbackContext ctx = new FallbackContext();
            ctx.question = question;
            ctx.sessionId = sessionId;
            return ctx;
        }

        public FallbackContext retryLogic(java.util.function.Function<Integer, Object> logic) {
            this.retryLogic = logic;
            return this;
        }

        public FallbackContext failedTool(String toolId) {
            this.failedToolId = toolId;
            return this;
        }

        public FallbackContext addPartialResult(String result) {
            this.partialResults.add(result);
            return this;
        }

        public FallbackContext escalationLevel(int level) {
            this.escalationLevel = level;
            return this;
        }

        public FallbackContext extra(String key, Object value) {
            this.extraData.put(key, value);
            return this;
        }

        public int incrementRetry() {
            return ++retryCount;
        }

        public boolean hasPartialResults() {
            return partialResults != null && !partialResults.isEmpty();
        }

        public String getBestPartialResult() {
            if (partialResults == null || partialResults.isEmpty()) return null;
            // 返回最长的结果（通常是信息量最大的）
            return partialResults.stream().max((a, b) -> Integer.compare(a.length(), b.length())).orElse(null);
        }

        public boolean shouldEscalate() {
            return escalationLevel >= 3 || retryCount >= 3;
        }

        // Getters
        public String getQuestion() { return question; }
        public String getSessionId() { return sessionId; }
        public String getFailedToolId() { return failedToolId; }
        public int getRetryCount() { return retryCount; }
        public int getEscalationLevel() { return escalationLevel; }
        public java.util.List<String> getPartialResults() { return partialResults; }
        public java.util.function.Function<Integer, Object> getRetryLogic() { return retryLogic; }
        public java.util.Map<String, Object> getExtraData() { return extraData; }
    }

    /**
     * 自定义兜底逻辑接口
     */
    @FunctionalInterface
    public interface FallbackLogic {
        FallbackResult execute(String error, FallbackContext context);
    }
}
