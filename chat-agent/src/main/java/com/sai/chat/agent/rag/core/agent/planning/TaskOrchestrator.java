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

package com.sai.chat.agent.rag.core.agent.planning;

import com.sai.chat.agent.rag.core.agent.state.ToolCallResult;
import com.sai.chat.agent.rag.core.mcp.MCPToolRegistry;
import com.sai.chat.agent.rag.core.mcp.RemoteMCPToolExecutor;
import com.sai.chat.agent.rag.core.pipeline.RAGPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 任务编排引擎
 * <p>
 * 负责按照 Plan 执行多步骤任务，支持：
 * <ul>
 *   <li>顺序执行（步骤间有依赖）</li>
 *   <li>并行执行（独立步骤可同时执行）</li>
 *   <li>步骤超时控制</li>
 *   <li>失败重试</li>
 *   <li>执行状态回调</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskOrchestrator {

    private final MCPToolRegistry toolRegistry;
    private final RemoteMCPToolExecutor toolExecutor;
    private final RAGPipelineService ragPipelineService;

    /**
     * 默认并行度
     */
    private static final int DEFAULT_PARALLELISM = 3;

    /**
     * 默认超时（毫秒）
     */
    private static final long DEFAULT_TIMEOUT = 120_000L;

    /**
     * 执行上下文（存放中间结果，供后续步骤引用）
     */
    private final Map<String, Object> executionContext = new ConcurrentHashMap<>();

    /**
     * 执行计划
     */
    private Plan currentPlan;

    /**
     * 执行回调
     */
    private TaskCallback callback;

    /**
     * 执行配置
     */
    private TaskConfig config;

    /**
     * 执行计划
     *
     * @param plan     执行计划
     * @param callback 状态回调（可传 null）
     * @return 执行后的计划（含结果）
     */
    public Plan execute(Plan plan, TaskCallback callback) {
        return execute(plan, TaskConfig.defaultConfig(), callback);
    }

    /**
     * 执行计划（带配置）
     *
     * @param plan   执行计划
     * @param config 执行配置
     * @return 执行后的计划（含结果）
     */
    public Plan execute(Plan plan, TaskConfig config, TaskCallback callback) {
        this.currentPlan = plan;
        this.callback = callback;
        this.config = config != null ? config : TaskConfig.defaultConfig();
        this.executionContext.clear();

        // 存入原始问题
        executionContext.put("original_question", plan.getOriginalQuestion());

        log.info("开始执行计划, planId={}, steps={}", plan.getPlanId(), plan.getStepCount());

        if (plan.getSteps() == null || plan.getSteps().isEmpty()) {
            log.warn("计划为空，无需执行");
            return plan;
        }

        try {
            // 初始化所有步骤状态
            for (PlanStep step : plan.getSteps()) {
                if (step.getStatus() == StepStatus.PENDING) {
                    step.markSkipped(); // 默认跳过，除非被依赖
                }
            }

            // 按拓扑顺序执行
            executeTopological(plan);

        } catch (Exception e) {
            log.error("计划执行异常, planId={}", plan.getPlanId(), e);
            handlePlanError(plan, e);
        }

        log.info("计划执行完成, planId={}, completed={}/{}",
                plan.getPlanId(), plan.getCompletedStepCount(), plan.getStepCount());

        return plan;
    }

    /**
     * 拓扑顺序执行（保证依赖顺序）
     */
    private void executeTopological(Plan plan) {
        List<PlanStep> steps = plan.getSteps();
        int maxIterations = steps.size() * 2; // 防止死循环
        int iteration = 0;

        while (!plan.isCompleted() && iteration < maxIterations) {
            iteration++;

            // 找所有可以执行的步骤
            List<PlanStep> readySteps = steps.stream()
                    .filter(step -> step.getStatus() == StepStatus.PENDING)
                    .filter(step -> step.isReady(steps))
                    .toList();

            if (readySteps.isEmpty()) {
                // 没有可执行的步骤，可能是依赖环或全部完成
                if (!plan.isCompleted()) {
                    log.warn("无可执行步骤但计划未完成，可能存在依赖环或死锁");
                    break;
                }
                break;
            }

            // 按并行组分组
            Map<String, List<PlanStep>> parallelGroups = new HashMap<>();
            List<PlanStep> sequentialSteps = new ArrayList<>();

            for (PlanStep step : readySteps) {
                if (step.isParallelizable() && step.getParallelGroup() != null) {
                    parallelGroups.computeIfAbsent(step.getParallelGroup(), k -> new ArrayList<>()).add(step);
                } else {
                    sequentialSteps.add(step);
                }
            }

            // 先执行顺序步骤
            for (PlanStep step : sequentialSteps) {
                executeStep(step, plan);
            }

            // 再执行并行组
            for (List<PlanStep> group : parallelGroups.values()) {
                executeParallel(group, plan);
            }
        }
    }

    /**
     * 顺序执行单个步骤
     */
    private void executeStep(PlanStep step, Plan plan) {
        log.info("执行步骤, stepId={}, name={}", step.getStepId(), step.getName());

        if (callback != null) {
            callback.onStepStart(step);
        }

        // 填充依赖的输出到参数
        resolveParameters(step, plan);

        try {
            ToolCallResult result = doExecute(step);

            if (result.isSuccess()) {
                step.markCompleted(result);
                // 存入执行上下文，供后续步骤使用
                executionContext.put("step" + step.getStepId() + "_output", result.getContent());
                executionContext.put("step" + step.getStepId() + "_result", result);

                log.info("步骤执行成功, stepId={}, duration={}ms",
                        step.getStepId(), step.getDurationMs());

                if (callback != null) {
                    callback.onStepComplete(step, result);
                }
            } else {
                handleStepFailure(step, result.getErrorMessage(), plan);
            }

        } catch (Exception e) {
            log.error("步骤执行异常, stepId={}", step.getStepId(), e);
            handleStepFailure(step, e.getMessage(), plan);
        }
    }

    /**
     * 并行执行一组步骤
     */
    private void executeParallel(List<PlanStep> steps, Plan plan) {
        if (steps == null || steps.isEmpty()) return;

        log.info("并行执行 {} 个步骤", steps.size());

        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(steps.size(), config.parallelism()));

        List<Future<ToolCallResult>> futures = new ArrayList<>();

        for (PlanStep step : steps) {
            Future<ToolCallResult> future = executor.submit(() -> {
                resolveParameters(step, plan);
                return doExecute(step);
            });
            futures.add(future);
        }

        // 收集结果
        int i = 0;
        for (PlanStep step : steps) {
            try {
                ToolCallResult result = futures.get(i).get(config.stepTimeoutMs(), TimeUnit.MILLISECONDS);
                if (result.isSuccess()) {
                    step.markCompleted(result);
                    executionContext.put("step" + step.getStepId() + "_output", result.getContent());
                    executionContext.put("step" + step.getStepId() + "_result", result);

                    if (callback != null) {
                        callback.onStepComplete(step, result);
                    }
                } else {
                    handleStepFailure(step, result.getErrorMessage(), plan);
                }
            } catch (TimeoutException e) {
                handleStepFailure(step, "步骤执行超时", plan);
            } catch (Exception e) {
                handleStepFailure(step, e.getMessage(), plan);
            }
            i++;
        }

        executor.shutdown();
    }

    /**
     * 填充步骤参数（解析变量引用）
     */
    private void resolveParameters(PlanStep step, Plan plan) {
        if (step.getParameters() == null) {
            step.setParameters(new java.util.HashMap<>());
        }

        // 如果有参数模板，解析变量
        if (step.getParameterTemplate() != null) {
            String resolved = resolveTemplate(step.getParameterTemplate(), plan);
            // 解析后的参数存入 parameters
            try {
                Object parsed = new com.google.gson.Gson().fromJson(resolved, Object.class);
                if (parsed instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resolvedParams = (Map<String, Object>) parsed;
                    step.getParameters().putAll(resolvedParams);
                }
            } catch (Exception e) {
                log.warn("参数模板解析失败: {}", e.getMessage());
            }
        }

        // 解析 parameters 中的变量引用
        for (Map.Entry<String, Object> entry : step.getParameters().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                String resolved = resolveTemplate((String) value, plan);
                entry.setValue(resolved);
            }
        }
    }

    /**
     * 解析变量模板
     * <p>
     * 支持格式: ${step1.output}, ${step2.result.field}, ${original_question}
     */
    private String resolveTemplate(String template, Plan plan) {
        if (template == null || !template.contains("${")) {
            return template;
        }

        String result = template;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = executionContext.get(varName);
            if (value == null) {
                // 尝试解析复杂路径，如 step1.output
                value = resolveNestedContext(varName);
            }
            String replacement = value != null ? String.valueOf(value) : "";
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private Object resolveNestedContext(String path) {
        // 支持 step1.output 格式
        if (path.startsWith("step")) {
            int dotIdx = path.indexOf('.');
            if (dotIdx > 0) {
                String stepKey = path.substring(0, dotIdx) + "_output";
                Object val = executionContext.get(stepKey);
                if (val != null && dotIdx < path.length() - 1) {
                    String remaining = path.substring(dotIdx + 1);
                    // 简单处理：如果是 JSON 字符串，尝试解析
                    try {
                        Object parsed = new com.google.gson.Gson().fromJson(val.toString(), Object.class);
                        if (parsed instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> json = (Map<String, Object>) parsed;
                            for (String k : remaining.split("\\.")) {
                                Object v = json.get(k);
                                if (v instanceof Map) {
                                    json = (Map<String, Object>) v;
                                } else {
                                    return v;
                                }
                            }
                            return json;
                        }
                        return parsed;
                    } catch (Exception e) {
                        return null;
                    }
                }
                return val;
            }
        }
        return executionContext.get(path);
    }

    /**
     * 实际执行业务逻辑
     */
    private ToolCallResult doExecute(PlanStep step) {
        long start = System.currentTimeMillis();

        return switch (step.getType()) {
            case TOOL_CALL -> executeToolCall(step, start);
            case QUERY -> executeQuery(step, start);
            case AGGREGATE -> executeAggregate(step, start);
            case DECISION -> executeDecision(step, start);
            case ANSWER -> executeAnswer(step, start);
        };
    }

    /**
     * 执行工具调用
     */
    private ToolCallResult executeToolCall(PlanStep step, long start) {
        if (step.getToolId() == null) {
            return ToolCallResult.failure("unknown", "未指定工具ID");
        }

        // 获取工具定义
        var toolOpt = toolRegistry.getTool(step.getToolId());
        if (toolOpt.isEmpty()) {
            return ToolCallResult.failure("unknown", "工具不存在: " + step.getToolId());
        }

        // 获取 Server 名称
        var serverNameOpt = toolRegistry.getServerName(step.getToolId());
        String serverName = serverNameOpt.orElse("default");

        // 执行工具
        String result = toolExecutor.execute(serverName, step.getToolId(), step.getParameters());

        long duration = System.currentTimeMillis() - start;
        if (result != null && !result.contains("失败") && !result.contains("error")) {
            return ToolCallResult.success(step.getToolId(), step.getToolName(), result, duration);
        } else {
            return ToolCallResult.failure(step.getToolId(), result, duration, null);
        }
    }

    /**
     * 执行 RAG 查询
     */
    private ToolCallResult executeQuery(PlanStep step, long start) {
        try {
            // 从上下文获取查询内容
            String query = resolveTemplate(
                    step.getParameters() != null ? (String) step.getParameters().get("query") : null,
                    currentPlan);

            if (query == null || query.isBlank()) {
                query = (String) executionContext.get("original_question");
            }

            // 执行 RAG 查询
            String result = ragPipelineService.chat(query, null);
            long duration = System.currentTimeMillis() - start;

            return ToolCallResult.success("rag_query", "RAG查询", result, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            return ToolCallResult.failure("rag_query", e.getMessage(), duration, null);
        }
    }

    /**
     * 执行聚合操作
     */
    private ToolCallResult executeAggregate(PlanStep step, long start) {
        // 收集所有已完成步骤的输出
        StringBuilder aggregated = new StringBuilder();
        for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
            if (entry.getKey().endsWith("_output") && entry.getValue() != null) {
                aggregated.append(entry.getValue()).append("\n\n");
            }
        }

        long duration = System.currentTimeMillis() - start;
        return ToolCallResult.success(step.getToolId(), step.getName(), aggregated.toString().trim(), duration);
    }

    /**
     * 执行决策
     */
    private ToolCallResult executeDecision(PlanStep step, long start) {
        // 收集上下文信息
        Map<String, Object> context = new HashMap<>();
        for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
            if (entry.getKey().endsWith("_result")) {
                context.put(entry.getKey(), entry.getValue());
            }
        }

        long duration = System.currentTimeMillis() - start;
        // 决策步骤标记为完成，实际决策由 LLM 在后续步骤完成
        return ToolCallResult.success(step.getToolId(), step.getName(),
                "决策上下文已准备，共 " + context.size() + " 条候选结果", duration);
    }

    /**
     * 执行回答生成
     */
    private ToolCallResult executeAnswer(PlanStep step, long start) {
        // 收集所有输出
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
            if (entry.getKey().endsWith("_output") && entry.getValue() != null) {
                content.append(entry.getValue()).append("\n");
            }
        }

        long duration = System.currentTimeMillis() - start;
        return ToolCallResult.success(step.getToolId(), step.getName(),
                "回答内容已准备好: " + content.length() + " 字符", duration);
    }

    /**
     * 处理步骤失败
     */
    private void handleStepFailure(PlanStep step, String errorMessage, Plan plan) {
        step.markFailed(errorMessage);

        log.warn("步骤执行失败, stepId={}, error={}", step.getStepId(), errorMessage);

        // 检查是否可重试
        if (step.canRetry() && config.enableRetry()) {
            step.setCurrentRetries(step.getCurrentRetries() + 1);
            step.setStatus(StepStatus.PENDING);
            log.info("步骤重试, stepId={}, retry={}/{}",
                    step.getStepId(), step.getCurrentRetries(), step.getMaxRetries());
        } else {
            // 标记依赖此步骤的后续步骤为跳过
            skipDependentSteps(step, plan);

            if (callback != null) {
                callback.onStepFailed(step, errorMessage);
            }
        }
    }

    /**
     * 跳过依赖失败步骤的后续步骤
     */
    private void skipDependentSteps(PlanStep failedStep, Plan plan) {
        for (PlanStep step : plan.getSteps()) {
            if (step.getDependsOn() != null && step.getDependsOn().contains(failedStep.getStepId())) {
                if (step.getStatus() == StepStatus.PENDING || step.getStatus() == StepStatus.WAITING) {
                    step.markSkipped();
                    log.info("因前置步骤失败而跳过步骤, stepId={}", step.getStepId());
                    // 递归跳过
                    skipDependentSteps(step, plan);
                }
            }
        }
    }

    /**
     * 处理计划级错误
     */
    private void handlePlanError(Plan plan, Exception e) {
        for (PlanStep step : plan.getSteps()) {
            if (step.getStatus() == StepStatus.RUNNING || step.getStatus() == StepStatus.PENDING) {
                step.markFailed("计划执行异常: " + e.getMessage());
            }
        }
    }

    /**
     * 获取当前执行上下文
     */
    public Map<String, Object> getExecutionContext() {
        return Collections.unmodifiableMap(executionContext);
    }
}
