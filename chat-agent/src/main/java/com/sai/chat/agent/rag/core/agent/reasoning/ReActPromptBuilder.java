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
            你是一个智能助手，可以通过调用工具来完成任务。

            ## 可用工具
            你可以调用以下工具来帮助你回答用户问题：

            %s

            ## 输出格式
            请严格按照以下格式输出你的推理过程和动作：

            Thought: [你的思考过程，分析用户问题，决定下一步行动]
            Action: [动作类型，TOOL_CALL 或 ANSWER 或 WAIT_INPUT]
            Action Input: [如果选择 TOOL_CALL，填写 {"toolId": "工具ID", "parameters": {...}} 的 JSON 格式]
            # 如果选择 ANSWER，填写 {"answer": "你的回答"}
            # 如果选择 WAIT_INPUT，填写 {"message": "你想要用户提供的补充信息"}

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
            记住：如果你已经收集到足够的信息来回答用户问题，请使用 ANSWER 动作。
            如果你需要用户提供更多信息才能继续，请使用 WAIT_INPUT 动作。
            """;

    /**
     * 构建初始 Prompt（第一次推理）
     *
     * @param question    用户问题
     * @param tools       可用工具列表
     * @param agentContext Agent 上下文摘要
     * @return 构建好的 ChatMessage 列表
     */
    public static List<ChatMessage> buildInitialPrompt(
            String question,
            List<MCPToolDefinition> tools,
            String agentContext) {

        String toolsDescription = formatToolsDescription(tools);
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, toolsDescription);

        if (agentContext != null && !agentContext.isBlank()) {
            systemPrompt = systemPrompt + "\n\n## Agent 上下文\n" + agentContext + "\n";
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        messages.add(ChatMessage.user(question));

        return messages;
    }

    /**
     * 构建后续推理 Prompt（基于历史）
     *
     * @param state           Agent 状态
     * @param observation     当前观察结果
     * @param question        用户问题
     * @return 构建好的 ChatMessage 列表
     */
    public static List<ChatMessage> buildContinuationPrompt(
            AgentState state,
            String observation,
            String question) {

        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统提示词
        String systemPrompt = """
                你是一个智能助手，正在通过推理和调用工具来回答用户问题。
                请基于历史推理轨迹和当前观察结果，继续推理过程。
                """;
        messages.add(ChatMessage.system(systemPrompt));

        // 添加历史轨迹
        String historyText = formatHistory(state);
        String historyBlock = historyText.isBlank() ? "（首次推理，无历史）" : historyText;

        // 构建带历史的输入
        String continuation = String.format(
                WITH_HISTORY_TEMPLATE,
                historyBlock,
                observation,
                question
        );

        messages.add(ChatMessage.user(continuation));

        return messages;
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
