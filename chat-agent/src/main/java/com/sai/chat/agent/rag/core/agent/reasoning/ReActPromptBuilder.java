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

package com.sai.chat.agent.rag.core.agent.reasoning;

import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.rag.core.agent.state.AgentState;
import com.sai.chat.agent.rag.core.mcp.MCPToolDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct Prompt 构造器
 * <p>
 * 构建符合 ReAct 格式的 Prompt，包含：
 * <ul>
 *   <li>工具定义列表</li>
 *   <li>历史推理轨迹</li>
 *   <li>当前观察结果</li>
 *   <li>用户问题</li>
 * </ul>
 * <p>
 * 该类只提供静态方法，不持有状态。
 */
public final class ReActPromptBuilder {

    private ReActPromptBuilder() {
        // 私有构造函数，防止实例化
    }

    /**
     * 系统提示词模板
     */
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个严格遵循 ReAct (Reasoning + Acting) 模式的智能助手。

            核心规则：你必须严格按照以下格式输出，**每一轮都必须输出 Thought 和 Action 两部分**。

            ## 可用工具
            你可以调用以下工具来帮助你回答用户问题：

            %s

            ## 输出格式（必须严格遵守）
            ```
            Thought: [你的思考过程，分析用户问题，决定下一步行动]
            Action: [TOOL_CALL | ANSWER]
            Action Input: [TOOL_CALL 时填写 {"toolId": "工具ID", "parameters": {...}} 的 JSON 格式]
                          [ANSWER 时填写 {"answer": "你的回答"} 的 JSON 格式]
            ```

            重要规则：
            - 如果有可用工具且问题需要信息检索，第一轮应先调用工具
            - 如果没有可用工具，或问题明显属于通用知识，直接使用 ANSWER
            - 绝对不能在没有工具可用时强行调用工具
            - ANSWER 的 content 中直接写出你要回复给用户的完整内容

            开始：
            """;

    /**
     * 带思考历史的 Prompt 模板（第二次及以后的推理）
     */
    private static final String WITH_HISTORY_TEMPLATE = """
            ## 历史推理轨迹
            %s

            ## 当前观察
            %s

            ## 用户问题
            %s

            请基于以上信息，继续你的推理过程。
            重要：必须使用以下格式输出，**不要直接输出答案**：
            ```
            Thought: [继续分析]
            Action: [TOOL_CALL | ANSWER]
            Action Input: [对应格式的 JSON]
            ```
            如果已收集到足够信息，使用 ANSWER；否则使用 TOOL_CALL 继续获取信息。
            """;

    /**
     * 构建初始 Prompt（第一次推理）
     *
     * @param question     用户问题
     * @param tools       可用工具列表
     * @param agentContext Agent 上下文摘要
     * @param memoryContext 多层记忆上下文（三层记忆整合后的文本）
     * @return 构建好的 ChatMessage 列表
     */
    public static List<ChatMessage> buildInitialPrompt(
            String question,
            List<MCPToolDefinition> tools,
            String agentContext,
            String memoryContext) {

        String toolsDescription = formatToolsDescription(tools);
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, toolsDescription);

        if (agentContext != null && !agentContext.isBlank()) {
            systemPrompt = systemPrompt + "\n\n## Agent 上下文\n" + agentContext + "\n";
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));

        // 追加记忆上下文作为用户消息的前缀
        String userMessage = buildUserMessageWithMemory(question, memoryContext);
        messages.add(ChatMessage.user(userMessage));

        return messages;
    }

    /**
     * 构建后续推理 Prompt（基于历史）
     *
     * @param state           Agent 状态
     * @param observation     当前观察结果
     * @param question        用户问题
     * @param memoryContext  多层记忆上下文
     * @return 构建好的 ChatMessage 列表
     */
    public static List<ChatMessage> buildContinuationPrompt(
            AgentState state,
            String observation,
            String question,
            String memoryContext) {

        List<ChatMessage> messages = new ArrayList<>();

        // 必须复用与初始 Prompt 一致的严格系统提示词，确保模型始终遵守格式
        String systemPrompt = """
                你是一个严格遵循 ReAct (Reasoning + Acting) 模式的智能助手。

                核心规则：你必须严格按照以下格式输出每一轮，**每一轮都必须输出 Thought 和 Action 两部分**。

                ## 输出格式（必须严格遵守）
                ```
                Thought: [你的思考过程，分析当前观察，决定下一步行动]
                Action: [TOOL_CALL | ANSWER]
                Action Input: [TOOL_CALL 时填写 {"toolId": "工具ID", "parameters": {...}} 的 JSON 格式]
                              [ANSWER 时填写 {"answer": "你的回答"} 的 JSON 格式]
                ```

                重要规则：
                - 如果已收集到足够信息，使用 ANSWER
                - 如果需要额外信息才能回答，使用 TOOL_CALL
                - ANSWER 的 content 中直接写出你要回复给用户的完整内容
                """;
        messages.add(ChatMessage.system(systemPrompt));

        var history = state.getConversationHistory();
        if (history != null && !history.isEmpty()) {
            for (ChatMessage msg : history) {
                messages.add(msg);
            }
        }

        String historyText = formatHistory(state);
        String historyBlock = historyText.isBlank() ? "（首次推理，无历史）" : historyText;

        // 追加记忆上下文
        String memoryBlock = (memoryContext != null && !memoryContext.isBlank())
                ? "\n## 历史记忆\n" + memoryContext + "\n"
                : "";

        String continuation = String.format(
                WITH_HISTORY_TEMPLATE,
                historyBlock,
                observation,
                question
        );

        messages.add(ChatMessage.user(continuation + memoryBlock));

        return messages;
    }

    /**
     * 将记忆上下文附加到用户消息前缀
     */
    private static String buildUserMessageWithMemory(String question, String memoryContext) {
        if (memoryContext == null || memoryContext.isBlank()) {
            return question;
        }
        return "## 历史记忆\n" + memoryContext + "\n\n## 当前问题\n" + question;
    }

    /**
     * 格式化工具描述
     */
    private static String formatToolsDescription(List<MCPToolDefinition> tools) {
        if (tools == null || tools.isEmpty()) {
            return "（当前无可用工具）";
        }

        StringBuilder sb = new StringBuilder();
        for (MCPToolDefinition tool : tools) {
            sb.append("- **").append(tool.getName()).append("** (`").append(tool.getToolId()).append("`)\n");
            sb.append("  描述：").append(tool.getDescription()).append("\n");

            if (tool.getParameters() != null && !tool.getParameters().isEmpty()) {
                sb.append("  参数：\n");
                for (var entry : tool.getParameters().entrySet()) {
                    var param = entry.getValue();
                    sb.append("    - ").append(entry.getKey())
                      .append(" (").append(param.getType()).append(")")
                      .append(": ").append(param.getDescription());
                    if (param.isRequired()) {
                        sb.append(" [必填]");
                    }
                    if (param.getEnumValues() != null && !param.getEnumValues().isEmpty()) {
                        sb.append("，可选值：").append(String.join(", ", param.getEnumValues()));
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化历史轨迹
     */
    private static String formatHistory(AgentState state) {
        if (state == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 添加历史思考
        var thoughts = state.getThoughtHistory();
        if (thoughts != null && !thoughts.isEmpty()) {
            for (int i = 0; i < thoughts.size(); i++) {
                sb.append("### 步骤 ").append(i + 1).append("\n");
                sb.append("Thought: ").append(thoughts.get(i)).append("\n");

                // 查找对应的工具调用
                var toolTrace = state.getToolCallTrace();
                if (toolTrace != null && i < toolTrace.size()) {
                    var record = toolTrace.get(i);
                    if (record.getToolId() != null) {
                        sb.append("Action: TOOL_CALL\n");
                        sb.append("Action Input: {\"toolId\": \"").append(record.getToolId()).append("\"");
                        if (record.getParameters() != null && !record.getParameters().isEmpty()) {
                            sb.append(", \"parameters\": ").append(formatParameters(record.getParameters()));
                        }
                        sb.append("}\n");
                    }
                }

                // 添加观察结果
                var observations = state.getObservationHistory();
                if (observations != null && i < observations.size()) {
                    sb.append("Observation: ").append(observations.get(i)).append("\n");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 格式化参数字典
     */
    private static String formatParameters(java.util.Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var entry : params.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else {
                sb.append(entry.getValue());
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
