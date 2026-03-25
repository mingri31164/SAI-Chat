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

package com.sai.chat.agent.rag.core.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.chat.agent.framework.convention.ChatMessage;
import com.sai.chat.agent.framework.convention.ChatRequest;
import com.sai.chat.agent.framework.convention.RetrievedChunk;
import com.sai.chat.agent.framework.convention.rag.GuidanceDecision;
import com.sai.chat.agent.framework.convention.rag.NodeScore;
import com.sai.chat.agent.framework.convention.rag.RewriteResult;
import com.sai.chat.agent.framework.web.SseEmitterSender;
import com.sai.chat.agent.infra.chat.LLMService;
import com.sai.chat.agent.infra.chat.StreamCancellationHandle;
import com.sai.chat.agent.rag.config.RAGProperties;
import com.sai.chat.agent.rag.constant.RAGConstant;
import com.sai.chat.agent.rag.core.intent.IntentClassifier;
import com.sai.chat.agent.rag.core.intent.IntentGuidanceService;
import com.sai.chat.agent.rag.core.memory.ConversationMemoryService;
import com.sai.chat.agent.rag.core.memory.SummaryGenerationService;
import com.sai.chat.agent.rag.core.mcp.LLMMCPParameterExtractor;
import com.sai.chat.agent.rag.core.mcp.MCPToolDefinition;
import com.sai.chat.agent.rag.core.mcp.MCPToolRegistry;
import com.sai.chat.agent.rag.core.mcp.RemoteMCPToolExecutor;
import com.sai.chat.agent.rag.core.retrieval.MultiChannelRetrievalEngine;
import com.sai.chat.agent.rag.core.retrieval.RerankService;
import com.sai.chat.agent.rag.core.rewrite.QueryRewriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RAG 流水线服务实现
 * <p>
 * 串联完整的 RAG 处理链路，支持同步和 SSE 流式两种调用方式。
 * <p>
 * SSE 流式链路：
 * <pre>
 * 1. 发送 INTENT 事件（意图分类结果）
 * 2. 发送 REWRITE 事件（查询改写结果）
 * 3. 发送 RETRIEVAL 事件（检索到的 Chunk 列表）
 * 4. 发送 THINKING/ANSWER 事件（LLM 思考过程 + 回复流）
 * 5. 发送 DONE 事件（完成）
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RAGPipelineServiceImpl implements RAGPipelineService {

    private final IntentClassifier intentClassifier;
    private final IntentGuidanceService intentGuidanceService;
    private final QueryRewriteService queryRewriteService;
    private final MultiChannelRetrievalEngine retrievalEngine;
    private final RerankService rerankService;
    private final LLMService llmService;
    private final ConversationMemoryService memoryService;
    private final SummaryGenerationService summaryService;
    private final RAGProperties ragProperties;
    private final ObjectMapper objectMapper;
    private final MCPToolRegistry mcpToolRegistry;
    private final RemoteMCPToolExecutor mcpToolExecutor;
    private final LLMMCPParameterExtractor mcpParamExtractor;

    // ================== 同步接口 ==================

    @Override
    public String chat(String question, String sessionId) {
        // 1. 意图分类
        List<NodeScore> nodeScores = intentClassifier.classifyTargets(question);

        // 2. 歧义检测
        GuidanceDecision guidance = intentGuidanceService.makeGuidanceDecision(question, nodeScores);
        if (guidance.isPrompt()) {
            return guidance.getPrompt();
        }

        // 3. 查询改写
        RewriteResult rewriteResult = queryRewriteService.rewrite(question, sessionId);

        // 4. MCP 工具调用路由
        if (ragProperties.getMcp().isEnabled() && !nodeScores.isEmpty()) {
            NodeScore top = nodeScores.get(0);
            if (top.getNode() != null && top.getNode().isMCP()) {
                String mcpAnswer = handleMCPTool(question, rewriteResult, top.getNode().getMcpToolId());
                memoryService.saveUserMessage(sessionId, question);
                memoryService.saveAssistantMessage(sessionId, mcpAnswer);
                if (memoryService.needSummary(sessionId)) {
                    summaryService.generateSummary(sessionId);
                }
                return mcpAnswer;
            }
        }

        // 5. 多路检索
        int topK = ragProperties.getSearch().getIntentDirected().getTopKMultiplier()
                * RAGConstant.DEFAULT_TOP_K;
        List<RetrievedChunk> chunks = retrievalEngine.retrieve(
                rewriteResult.getRewrittenQuestion(), sessionId, topK);

        // 6. 重排序（可选）
        if (rerankService != null && !chunks.isEmpty()) {
            chunks = rerankService.rerank(rewriteResult.getRewrittenQuestion(), chunks, RAGConstant.DEFAULT_TOP_K);
        }

        // 7. LLM 生成
        String contextText = buildContextText(chunks);
        String answer = callLLM(question, rewriteResult, contextText, sessionId);

        // 8. 保存对话记忆
        memoryService.saveUserMessage(sessionId, question);
        memoryService.saveAssistantMessage(sessionId, answer);

        // 9. 摘要触发检查
        if (memoryService.needSummary(sessionId)) {
            summaryService.generateSummary(sessionId);
        }

        return answer;
    }

    @Override
    public GuidanceDecision checkGuidance(String question, String sessionId) {
        List<NodeScore> nodeScores = intentClassifier.classifyTargets(question);
        return intentGuidanceService.makeGuidanceDecision(question, nodeScores);
    }

    // ================== SSE 流式接口 ==================

    /**
     * 执行 SSE 流式 RAG 流水线
     *
     * @param question   用户问题
     * @param sessionId  会话 ID
     * @param deepThinking 是否启用深度思考
     * @param sender     SSE 发送器
     * @return StreamCancellationHandle，用于取消推理
     */
    public StreamCancellationHandle streamChat(
            String question,
            String sessionId,
            boolean deepThinking,
            SseEmitterSender sender) {

        AtomicBoolean cancelled = new AtomicBoolean(false);

        try {
            // ===== 阶段 1: 意图分类 =====
            List<NodeScore> nodeScores = intentClassifier.classifyTargets(question);
            sendJson(sender, SSEventType.INTENT, buildIntentEvent(nodeScores));

            // ===== 阶段 2: 歧义检测 =====
            GuidanceDecision guidance = intentGuidanceService.makeGuidanceDecision(question, nodeScores);
            if (guidance.isPrompt()) {
                sendJson(sender, SSEventType.GUIDANCE, buildGuidanceEvent(guidance, nodeScores));
                sender.complete();
                return () -> cancelled.set(true);
            }

            // ===== 阶段 3: 查询改写 =====
            RewriteResult rewriteResult = queryRewriteService.rewrite(question, sessionId);
            sendJson(sender, SSEventType.REWRITE, rewriteResult);

            // ===== 阶段 4: 多路检索 =====
            int topK = computeTopK();
            List<RetrievedChunk> chunks = retrievalEngine.retrieve(
                    rewriteResult.getRewrittenQuestion(), sessionId, topK);
            sendJson(sender, SSEventType.RETRIEVAL, buildRetrievalEvent(chunks));

            // ===== 阶段 5: LLM 流式生成 =====
            String contextText = buildContextText(chunks);
            RAGStreamCallback callback = new RAGStreamCallback(sender);

            ChatRequest llmRequest = buildLLMRequest(question, rewriteResult, contextText, sessionId, deepThinking);

            StreamCancellationHandle handle = llmService.streamChat(llmRequest, callback);

            // ===== 阶段 6: 保存对话记忆（在 LLM 回复完成后） =====
            Thread saveThread = new Thread(() -> {
                try {
                    callback.awaitCompletion();
                    memoryService.saveUserMessage(sessionId, question);
                    memoryService.saveAssistantMessage(sessionId, callback.getFullContent());
                    if (memoryService.needSummary(sessionId)) {
                        summaryService.generateSummary(sessionId);
                    }
                } catch (Exception e) {
                    log.warn("保存对话记忆失败, sessionId={}", sessionId, e);
                }
            }, "memory-save");
            saveThread.start();

            return () -> {
                cancelled.set(true);
                handle.cancel();
            };

        } catch (Exception e) {
            log.error("SSE RAG 流水线异常, question={}", question, e);
            sendJson(sender, SSEventType.ERROR, Map.of("message", e.getMessage()));
            sender.complete();
            return () -> cancelled.set(true);
        }
    }

    // ================== 内部工具方法 ==================

    private String buildContextText(List<RetrievedChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk chunk = chunks.get(i);
            sb.append("【文档 ").append(i + 1).append("】\n")
              .append(chunk.getText()).append("\n\n");
        }
        return sb.toString();
    }

    private String callLLM(String question, RewriteResult rewrite, String contextText, String sessionId) {
        String prompt = buildPrompt(question, rewrite, contextText, sessionId);
        ChatRequest request = ChatRequest.builder()
                .messages(List.of(ChatMessage.user(prompt)))
                .temperature(0.7D)
                .thinking(false)
                .build();
        return llmService.chat(request);
    }

    private ChatRequest buildLLMRequest(String question, RewriteResult rewrite,
                                        String contextText, String sessionId, boolean deepThinking) {
        String prompt = buildPrompt(question, rewrite, contextText, sessionId);
        return ChatRequest.builder()
                .messages(List.of(ChatMessage.user(prompt)))
                .temperature(0.7D)
                .thinking(deepThinking)
                .build();
    }

    private String buildPrompt(String question, RewriteResult rewrite, String contextText, String sessionId) {
        StringBuilder sb = new StringBuilder();

        if (!contextText.isBlank()) {
            sb.append("【参考知识】\n").append(contextText).append("\n");
        }

        String summary = memoryService.getSummary(sessionId);
        if (summary != null && !summary.isBlank()) {
            sb.append("【对话摘要】\n").append(summary).append("\n\n");
        }

        sb.append("【用户问题】\n").append(question);

        return sb.toString();
    }

    private int computeTopK() {
        int multiplier = ragProperties.getSearch().getIntentDirected().getTopKMultiplier();
        return Math.max(RAGConstant.DEFAULT_TOP_K * multiplier, RAGConstant.MIN_SEARCH_TOP_K);
    }

    private void sendJson(SseEmitterSender sender, String eventType, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            sender.sendEvent(eventType, json);
        } catch (Exception e) {
            log.warn("SSE JSON 发送失败, event={}", eventType, e);
        }
    }

    private Map<String, Object> buildIntentEvent(List<NodeScore> nodeScores) {
        double topScore = nodeScores.isEmpty() ? 0.0 : nodeScores.get(0).getScore();
        return Map.of("scores", nodeScores, "topScore", topScore);
    }

    private Map<String, Object> buildGuidanceEvent(GuidanceDecision guidance, List<NodeScore> nodeScores) {
        return Map.of(
                "prompt", guidance.getPrompt(),
                "options", extractOptions(nodeScores)
        );
    }

    private Map<String, Object> buildRetrievalEvent(List<RetrievedChunk> chunks) {
        return Map.of("count", chunks.size(), "chunks", chunks);
    }

    private List<Map<String, Object>> extractOptions(List<NodeScore> nodeScores) {
        if (nodeScores == null || nodeScores.isEmpty()) {
            return List.of();
        }
        return nodeScores.stream()
                .limit(ragProperties.getGuidance().getMaxOptions())
                .map(ns -> Map.<String, Object>of(
                        "id", ns.getNode().getId(),
                        "name", ns.getNode().getName(),
                        "score", ns.getScore()
                ))
                .toList();
    }

    // ================== MCP 工具调用 ==================

    /**
     * 执行 MCP 工具调用并生成回答
     */
    private String handleMCPTool(String question, RewriteResult rewrite, String toolId) {
        if (toolId == null || toolId.isBlank()) {
            return "无法确定要调用的 MCP 工具";
        }

        try {
            // 1. 获取工具定义
            var toolOpt = mcpToolRegistry.getTool(toolId);
            if (toolOpt.isEmpty()) {
                return "MCP 工具 [" + toolId + "] 未找到";
            }

            MCPToolDefinition tool = toolOpt.get();

            // 2. LLM 参数提取
            Map<String, Object> params;
            if (ragProperties.getMcp().isLlmParameterExtract()) {
                params = mcpParamExtractor.extractParameters(tool, question);
            } else {
                params = Map.of();
            }

            // 3. 调用远程 MCP 工具
            String serverName = ragProperties.getMcp().getServerName();
            String toolResult = mcpToolExecutor.execute(serverName, toolId, params);

            // 4. 将工具结果转换为自然语言回答
            return buildMCPAnswer(question, toolResult, tool);

        } catch (Exception e) {
            log.error("MCP 工具调用失败, toolId={}", toolId, e);
            return "MCP 工具 [" + toolId + "] 调用失败: " + e.getMessage();
        }
    }

    /**
     * 将 MCP 工具结果转换为自然语言回答
     */
    private String buildMCPAnswer(String question, String toolResult, MCPToolDefinition tool) {
        String prompt = buildMCPAnswerPrompt(question, toolResult, tool);

        try {
            return llmService.chat(ChatRequest.builder()
                    .messages(List.of(ChatMessage.user(prompt)))
                    .temperature(0.3D)
                    .thinking(false)
                    .build());
        } catch (Exception e) {
            log.warn("MCP 结果转换失败，直接返回原始结果", e);
            return toolResult;
        }
    }

    private String buildMCPAnswerPrompt(String question, String toolResult, MCPToolDefinition tool) {
        return "【工具名称】\n" + tool.getName() + "\n\n" +
               "【工具执行结果】\n" + toolResult + "\n\n" +
               "【用户原始问题】\n" + question + "\n\n" +
               "请根据上述工具执行结果，用简洁自然的方式回答用户问题。";
    }
}
