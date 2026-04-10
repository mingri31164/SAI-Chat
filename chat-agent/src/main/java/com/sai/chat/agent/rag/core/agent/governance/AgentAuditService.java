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

package com.sai.chat.agent.rag.core.agent.governance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 审计服务
 * <p>
 * 负责记录和查询 Agent 执行审计信息。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentAuditService {

    /**
     * 审计记录存储（内存实现，生产环境应替换为数据库或 Elasticsearch）
     * key: auditId
     */
    private final Map<String, AgentAuditRecord> auditRecords = new ConcurrentHashMap<>();

    /**
     * 按用户索引
     */
    private final Map<String, String> userAuditIndex = new ConcurrentHashMap<>();

    /**
     * 按会话索引
     */
    private final Map<String, String> sessionAuditIndex = new ConcurrentHashMap<>();

    /**
     * 创建新的审计记录
     */
    public AgentAuditRecord startAudit(String userId, String sessionId,
                                       AgentPermission permission, String question) {
        AgentAuditRecord record = AgentAuditRecord.create(userId, sessionId, permission, question);
        auditRecords.put(record.getAuditId(), record);
        userAuditIndex.put(userId + ":" + record.getAuditId(), record.getAuditId());
        sessionAuditIndex.put(sessionId + ":" + record.getAuditId(), record.getAuditId());
        log.info("审计记录创建, auditId={}, userId={}, question={}",
                record.getAuditId(), userId, question);
        return record;
    }

    /**
     * 标记审计记录为拒绝
     */
    public void recordRejection(AgentAuditRecord record, String reason) {
        record.setStatus(AgentAuditRecord.AuditStatus.REJECTED);
        record.setRejectReason(reason);
        record.setEndTime(Instant.now());
        record.calculateDuration();
        log.info("审计记录标记为拒绝, auditId={}, reason={}", record.getAuditId(), reason);
    }

    /**
     * 标记审计记录为成功
     */
    public void recordSuccess(AgentAuditRecord record, String resultSummary,
                              int stepCount, int toolCallCount, int tokensUsed, double costUsed) {
        record.setStatus(AgentAuditRecord.AuditStatus.SUCCESS);
        record.setEndTime(Instant.now());
        record.setResultSummary(resultSummary);
        record.setStepCount(stepCount);
        record.setToolCallCount(toolCallCount);
        record.setTokensUsed(tokensUsed);
        record.setCostUsed(costUsed);
        record.calculateDuration();
        log.info("审计记录完成, auditId={}, duration={}ms, tokens={}",
                record.getAuditId(), record.getDurationMs(), tokensUsed);
    }

    /**
     * 标记审计记录为失败
     */
    public void recordFailure(AgentAuditRecord record, String errorMessage) {
        record.setStatus(AgentAuditRecord.AuditStatus.FAILED);
        record.setErrorMessage(errorMessage);
        record.setEndTime(Instant.now());
        record.calculateDuration();
        log.error("审计记录失败, auditId={}, error={}", record.getAuditId(), errorMessage);
    }

    /**
     * 标记审计记录为超时
     */
    public void recordTimeout(AgentAuditRecord record) {
        record.setStatus(AgentAuditRecord.AuditStatus.TIMEOUT);
        record.setEndTime(Instant.now());
        record.calculateDuration();
        log.warn("审计记录超时, auditId={}, duration={}ms", record.getAuditId(), record.getDurationMs());
    }

    /**
     * 标记审计记录为配额超限
     */
    public void recordQuotaExceeded(AgentAuditRecord record, String reason) {
        record.setStatus(AgentAuditRecord.AuditStatus.QUOTA_EXCEEDED);
        record.setRejectReason(reason);
        record.setEndTime(Instant.now());
        record.calculateDuration();
        log.warn("审计记录配额超限, auditId={}, reason={}", record.getAuditId(), reason);
    }

    /**
     * 更新审计记录的额外上下文
     */
    public void updateContext(AgentAuditRecord record, Map<String, Object> context) {
        record.setExtraContext(context);
    }

    /**
     * 查询审计记录
     */
    public Optional<AgentAuditRecord> getAuditRecord(String auditId) {
        return Optional.ofNullable(auditRecords.get(auditId));
    }

    /**
     * 查询用户最近的审计记录
     */
    public List<AgentAuditRecord> getRecentAuditsByUser(String userId, int limit) {
        return auditRecords.values().stream()
                .filter(r -> userId.equals(r.getUserId()))
                .sorted((a, b) -> {
                    Instant ta = a.getStartTime() != null ? a.getStartTime() : Instant.MIN;
                    Instant tb = b.getStartTime() != null ? b.getStartTime() : Instant.MIN;
                    return tb.compareTo(ta); // 倒序
                })
                .limit(limit)
                .toList();
    }

    /**
     * 查询会话的审计记录
     */
    public List<AgentAuditRecord> getAuditsBySession(String sessionId) {
        return auditRecords.values().stream()
                .filter(r -> sessionId.equals(r.getSessionId()))
                .sorted((a, b) -> {
                    Instant ta = a.getStartTime() != null ? a.getStartTime() : Instant.MIN;
                    Instant tb = b.getStartTime() != null ? b.getStartTime() : Instant.MIN;
                    return ta.compareTo(tb);
                })
                .toList();
    }

    /**
     * 查询包含敏感操作的审计记录
     */
    public List<AgentAuditRecord> getSensitiveAudits(int limit) {
        return auditRecords.values().stream()
                .filter(AgentAuditRecord::isContainsSensitiveOp)
                .sorted((a, b) -> {
                    Instant ta = a.getStartTime() != null ? a.getStartTime() : Instant.MIN;
                    Instant tb = b.getStartTime() != null ? b.getStartTime() : Instant.MIN;
                    return tb.compareTo(ta);
                })
                .limit(limit)
                .toList();
    }

    /**
     * 获取审计统计
     */
    public AuditStatistics getStatistics() {
        List<AgentAuditRecord> all = List.copyOf(auditRecords.values());
        long successCount = all.stream().filter(r -> r.getStatus() == AgentAuditRecord.AuditStatus.SUCCESS).count();
        long failedCount = all.stream().filter(r -> r.getStatus() == AgentAuditRecord.AuditStatus.FAILED).count();
        long rejectedCount = all.stream().filter(r -> r.getStatus() == AgentAuditRecord.AuditStatus.REJECTED).count();
        long totalTokens = all.stream().mapToLong(r -> r.getTokensUsed()).sum();
        double totalCost = all.stream().mapToDouble(r -> r.getCostUsed()).sum();
        double avgDuration = all.stream().mapToLong(r -> r.getDurationMs()).average().orElse(0);

        return new AuditStatistics(
                all.size(), successCount, failedCount, rejectedCount,
                totalTokens, totalCost, avgDuration
        );
    }

    /**
     * 审计统计
     */
    public record AuditStatistics(
            long totalCount,
            long successCount,
            long failedCount,
            long rejectedCount,
            long totalTokens,
            double totalCost,
            double avgDurationMs
    ) {
        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount : 0;
        }
    }
}
