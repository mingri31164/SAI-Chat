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

import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.rag.core.mcp.MCPToolDefinition;
import com.sai.chat.agent.rag.core.mcp.MCPToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 计划生成器
 * <p>
 * 将用户复杂问题分解为可执行的步骤计划。
 * <p>
 * 支持三种策略：
 * <ul>
 *   <li>简单任务 → 单步计划（RAG 或直接工具调用）</li>
 *   <li>多步任务 → 顺序计划（步骤间有依赖）</li>
 *   <li>探索任务 → 并行计划（多方向同时探索）</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanGenerator {

    private static final int MAX_STEPS = 10;
    private static final int MAX_TOOLS_IN_PROMPT = 20;

    private final LLMService llmService;
    private final MCPToolRegistry toolRegistry;

    /**
     * 生成执行计划
     *
     * @param question       用户问题
     * @param maxSteps      最大步骤数限制
     * @return 执行计划
     */
    public Plan generatePlan(String question, int maxSteps) {
        List<MCPToolDefinition> tools = getAvailableTools();
        if (tools == null || tools.isEmpty()) {
            // 没有可用工具，生成单步 RAG 计划
            return buildSingleStepPlan(question, StepType.QUERY);
        }

        // 优先尝试 LLM 分解
        Plan llmPlan = generatePlanWithLLM(question, tools, Math.min(maxSteps, MAX_STEPS));
        if (llmPlan != null && llmPlan.isExecutable()) {
            return llmPlan;
        }

        // LLM 失败时使用规则匹配
        return generatePlanByRules(question, tools);
    }

    /**
     * 使用 LLM 生成计划
     */
    private Plan generatePlanWithLLM(String question, List<MCPToolDefinition> tools, int maxSteps) {
        try {
            String prompt = buildPlanPrompt(question, tools, maxSteps);
            String response = llmService.chat(ChatRequest.builder()
                    .messages(List.of(ChatMessage.user(prompt)))
                    .temperature(0.1)
                    .thinking(false)
                    .build());

            return parsePlanFromLLM(question, response, tools);

        } catch (Exception e) {
            log.warn("LLM 计划生成失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使用规则匹配生成计划
     */
    private Plan generatePlanByRules(String question, List<MCPToolDefinition> tools) {
        // 检查是否需要多工具
        boolean needsMultipleTools = detectMultiToolQuestion(question);

        if (!needsMultipleTools) {
            // 简单问题，找单个最匹配的工具
            MCPToolDefinition bestTool = findBestMatchingTool(question, tools);
            if (bestTool != null) {
                return buildSingleStepPlan(question, StepType.TOOL_CALL, bestTool);
            }
            return buildSingleStepPlan(question, StepType.QUERY);
        }

        // 复杂问题，按规则分解
        return buildMultiStepPlanByRules(question, tools);
    }

    // ==================== Prompt 构建 ====================

    private String buildPlanPrompt(String question, List<MCPToolDefinition> tools, int maxSteps) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个任务规划助手。请将用户问题分解为执行步骤。\n\n");

        sb.append("【可用工具】\n");
        int count = 0;
        for (MCPToolDefinition tool : tools) {
            if (count++ >= MAX_TOOLS_IN_PROMPT) break;
            sb.append("- ").append(tool.getToolId()).append(": ")
              .append(tool.getName()).append(" - ").append(tool.getDescription()).append("\n");
        }
        sb.append("\n");

        sb.append("【输出格式】\n");
        sb.append("请返回 JSON 格式的计划：\n");
        sb.append("{\n");
        sb.append("  \"objective\": \"任务目标描述\",\n");
        sb.append("  \"steps\": [\n");
        sb.append("    {\n");
        sb.append("      \"stepId\": 1,\n");
        sb.append("      \"type\": \"TOOL_CALL|QUERY|AGGREGATE\",\n");
        sb.append("      \"toolId\": \"工具ID\",\n");
        sb.append("      \"description\": \"步骤描述\",\n");
        sb.append("      \"dependsOn\": [],\n");
        sb.append("      \"parallelGroup\": \"group1\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");

        sb.append("【用户问题】\n").append(question).append("\n\n");
        sb.append("【规则】\n");
        sb.append("- 步骤数不超过 ").append(maxSteps).append(" 个\n");
        sb.append("- 如果问题可以用一个工具回答，用单步计划\n");
        sb.append("- 如果需要多个工具，按依赖关系排序\n");
        sb.append("- 探索性问题（如「分析竞品」）可使用并行计划\n");
        sb.append("- 汇总类问题（如「生成报告」）放在最后一步\n");

        return sb.toString();
    }

    // ==================== LLM 结果解析 ====================

    private Plan parsePlanFromLLM(String question, String llmResponse, List<MCPToolDefinition> tools) {
        try {
            String jsonStr = extractJsonFromResponse(llmResponse);
            com.google.gson.JsonObject json = 
                    new com.google.gson.Gson().fromJson(jsonStr, com.google.gson.JsonObject.class);

            Plan.PlanBuilder builder = Plan.builder()
                    .planId(UUID.randomUUID().toString())
                    .originalQuestion(question);

            if (json.has("objective")) {
                builder.objective(json.get("objective").getAsString());
            }

            if (json.has("steps") && json.get("steps").isJsonArray()) {
                List<PlanStep> steps = new ArrayList<>();
                com.google.gson.JsonArray stepsArray = json.getAsJsonArray("steps");
                int stepId = 1;
                for (var el : stepsArray) {
                    if (!el.isJsonObject()) continue;
                    com.google.gson.JsonObject stepObj = el.getAsJsonObject();

                    PlanStep.PlanStepBuilder stepBuilder = PlanStep.builder()
                            .stepId(stepId++);

                    if (stepObj.has("type")) {
                        String type = stepObj.get("type").getAsString().toUpperCase();
                        stepBuilder.type(parseStepType(type));
                    }

                    if (stepObj.has("toolId")) {
                        stepBuilder.toolId(stepObj.get("toolId").getAsString());
                    }

                    if (stepObj.has("description")) {
                        stepBuilder.description(stepObj.get("description").getAsString());
                    }

                    if (stepObj.has("dependsOn") && stepObj.get("dependsOn").isJsonArray()) {
                        List<Integer> deps = new ArrayList<>();
                        for (var d : stepObj.getAsJsonArray("dependsOn")) {
                            deps.add(d.getAsInt());
                        }
                        stepBuilder.dependsOn(deps);
                    }

                    if (stepObj.has("parallelGroup")) {
                        stepBuilder.parallelGroup(stepObj.get("parallelGroup").getAsString());
                        stepBuilder.parallelizable(true);
                    }

                    // 填充工具名称
                    if (stepObj.has("toolId")) {
                        String toolId = stepObj.get("toolId").getAsString();
                        stepBuilder.toolName(toolId);
                    }

                    steps.add(stepBuilder.build());
                }
                builder.steps(steps);
                builder.executable(true);
            }

            return builder.build();

        } catch (Exception e) {
            log.warn("解析 LLM 计划失败: {}", e.getMessage());
            return null;
        }
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.isBlank()) {
            return "{}";
        }
        String trimmed = response.trim();

        // 尝试直接解析
        if (trimmed.startsWith("{")) {
            // 查找结束花括号
            int depth = 0;
            for (int i = 0; i < trimmed.length(); i++) {
                if (trimmed.charAt(i) == '{') depth++;
                else if (trimmed.charAt(i) == '}') {
                    depth--;
                    if (depth == 0) {
                        return trimmed.substring(0, i + 1);
                    }
                }
            }
        }

        // 尝试从 markdown 代码块中提取
        Pattern pattern = Pattern.compile("```(?:json)?\\s*(\\{.*\\})\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "{}";
    }

    private StepType parseStepType(String type) {
        return switch (type) {
            case "TOOL_CALL", "CALL", "TOOL" -> StepType.TOOL_CALL;
            case "QUERY", "SEARCH", "RETRIEVAL", "RAG" -> StepType.QUERY;
            case "AGGREGATE", "MERGE", "COMBINE" -> StepType.AGGREGATE;
            case "DECISION", "BRANCH" -> StepType.DECISION;
            case "ANSWER", "RESPONSE", "FINAL" -> StepType.ANSWER;
            default -> StepType.TOOL_CALL;
        };
    }

    // ==================== 规则匹配 ====================

    private boolean detectMultiToolQuestion(String question) {
        String q = question.toLowerCase();
        // 多工具关键词
        String[] multiKeywords = {
                "比较", "对比", "分析", "对比分析",
                "而且", "并且", "以及",
                "首先", "然后", "最后",
                "多个", "不同", "分别"
        };
        for (String kw : multiKeywords) {
            if (q.contains(kw)) return true;
        }
        return false;
    }

    private MCPToolDefinition findBestMatchingTool(String question, List<MCPToolDefinition> tools) {
        String q = question.toLowerCase();
        MCPToolDefinition best = null;
        int bestScore = 0;

        for (MCPToolDefinition tool : tools) {
            int score = calculateMatchScore(q, tool);
            if (score > bestScore) {
                bestScore = score;
                best = tool;
            }
        }

        // 匹配分数阈值
        return bestScore >= 1 ? best : null;
    }

    private int calculateMatchScore(String question, MCPToolDefinition tool) {
        int score = 0;
        String name = tool.getName().toLowerCase();
        String desc = (tool.getDescription() != null ? tool.getDescription() : "").toLowerCase();
        String q = question.toLowerCase();

        // 名称精确匹配
        if (name.contains(q) || q.contains(name)) score += 5;

        // 名称关键词匹配
        for (String kw : q.split("[\\s，。、,]+")) {
            if (kw.length() < 2) continue;
            if (name.contains(kw)) score += 2;
            if (desc.contains(kw)) score += 1;
        }
        return score;
    }

    private Plan buildSingleStepPlan(String question, StepType type) {
        return buildSingleStepPlan(question, type, null);
    }

    private Plan buildSingleStepPlan(String question, StepType type, MCPToolDefinition tool) {
        PlanStep step = PlanStep.builder()
                .stepId(1)
                .name("执行" + (tool != null ? tool.getName() : "查询"))
                .description(question)
                .type(type)
                .toolId(tool != null ? tool.getToolId() : null)
                .toolName(tool != null ? tool.getName() : null)
                .status(StepStatus.PENDING)
                .build();

        return Plan.builder()
                .planId(UUID.randomUUID().toString())
                .objective("回答: " + question)
                .originalQuestion(question)
                .type(PlanType.SINGLE_STEP)
                .steps(List.of(step))
                .executable(true)
                .build();
    }

    private Plan buildMultiStepPlanByRules(String question, List<MCPToolDefinition> tools) {
        List<PlanStep> steps = new ArrayList<>();
        int stepId = 1;

        // 按规则分解
        String q = question.toLowerCase();

        // 判断需要哪些工具
        List<MCPToolDefinition> neededTools = new ArrayList<>();
        for (MCPToolDefinition tool : tools) {
            if (calculateMatchScore(q, tool) >= 1) {
                neededTools.add(tool);
            }
        }

        if (neededTools.isEmpty()) {
            // 没匹配到工具，用 RAG
            steps.add(buildStep(stepId++, StepType.QUERY, null, null, "检索知识库", null));
        } else {
            for (MCPToolDefinition tool : neededTools) {
                steps.add(buildStep(stepId++, StepType.TOOL_CALL,
                        tool.getToolId(), tool.getName(),
                        "调用" + tool.getName(), null));
            }
        }

        // 最后一步汇总
        steps.add(buildStep(stepId, StepType.ANSWER, null, null,
                "汇总结果生成回答", extractDepends(steps)));

        return Plan.builder()
                .planId(UUID.randomUUID().toString())
                .objective("回答: " + question)
                .originalQuestion(question)
                .type(PlanType.PLAN_THEN_EXECUTE)
                .steps(steps)
                .executable(true)
                .build();
    }

    private PlanStep buildStep(int stepId, StepType type, String toolId, String toolName,
                               String description, List<Integer> dependsOn) {
        return PlanStep.builder()
                .stepId(stepId)
                .name(description)
                .description(description)
                .type(type)
                .toolId(toolId)
                .toolName(toolName)
                .dependsOn(dependsOn)
                .status(StepStatus.PENDING)
                .build();
    }

    private List<Integer> extractDepends(List<PlanStep> steps) {
        if (steps == null || steps.isEmpty()) return List.of();
        return List.of(steps.get(steps.size() - 1).getStepId());
    }

    private List<MCPToolDefinition> getAvailableTools() {
        try {
            return toolRegistry.getAllTools();
        } catch (Exception e) {
            log.warn("获取工具列表失败: {}", e.getMessage());
            return List.of();
        }
    }
}
