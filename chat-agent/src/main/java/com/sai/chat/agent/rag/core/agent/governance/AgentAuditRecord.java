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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Agent 执行审计记录
 * <p>
 * 记录每次 Agent 调用的完整执行链路，用于合规审计和故障追踪。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentAuditRecord {

    /**
     * 审计记录唯一标识
     */
    private String auditId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户权限等级
     */
    private AgentPermission permission;

    /**
     * 用户问题
     */
    private String question;

    /**
     * 执行状态
     */
    private AuditStatus status;

    /**
     * Agent 执行模式
     */
    private String agentMode;

    /**
     * 执行开始时间
     */
    private Instant startTime;

    /**
     * 执行结束时间
     */
    private Instant endTime;

    /**
     * 执行耗时（毫秒）
     */
    private long durationMs;

    /**
     * 执行步骤数
     */
    private int stepCount;

    /**
     * 工具调用次数
     */
    private int toolCallCount;

    /**
     * Token 消耗
     */
    private int tokensUsed;

    /**
     * 成本消耗（元）
     */
    private double costUsed;

    /**
     * 是否包含敏感操作
     */
    private boolean containsSensitiveOp;

    /**
     * 拒绝原因（当 status 为 REJECTED 时）
     */
    private String rejectReason;

    /**
     * 错误信息（当 status 为 FAILED 时）
     */
    private String errorMessage;

    /**
     * 调用结果摘要
     */
    private String resultSummary;

    /**
     * 额外上下文
     */
    private Map<String, Object> extraContext;

    /**
     * 审计状态
     */
    public enum AuditStatus {
        /**
         * 已批准执行
         */
        APPROVED,

        /**
         * 已拒绝（权限/配额不足）
         */
        REJECTED,

        /**
         * 执行中
         */
        IN_PROGRESS,

        /**
         * 成功完成
         */
        SUCCESS,

        /**
         * 执行失败
         */
        FAILED,

        /**
         * 超时终止
         */
        TIMEOUT,

        /**
         * 配额超限终止
         */
        QUOTA_EXCEEDED
    }

    /**
     * 计算执行时长
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }

    /**
     * 创建新的审计记录
     */
    public static AgentAuditRecord create(String userId, String sessionId,
                                          AgentPermission permission, String question) {
        return AgentAuditRecord.builder()
                .auditId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .sessionId(sessionId)
                .permission(permission)
                .question(question)
                .status(AuditStatus.IN_PROGRESS)
                .startTime(Instant.now())
                .build();
    }
}
