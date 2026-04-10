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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 人工升级服务
 * <p>
 * 当 Agent 无法自动处理时，将任务升级给人工处理。
 */
@Slf4j
@Service
public class EscalationService {

    /**
     * 待处理升级单
     */
    private final Map<String, EscalationTicket> pendingTickets = new ConcurrentHashMap<>();

    /**
     * 已处理升级单
     */
    private final Map<String, EscalationTicket> processedTickets = new ConcurrentHashMap<>();

    /**
     * 升级通知回调
     */
    private volatile EscalationNotifier notifier = (ticket) -> {
        log.info("升级通知: ticketId={}, question={}", ticket.getTicketId(), ticket.getQuestion());
    };

    /**
     * 创建升级单
     *
     * @param context 升级上下文
     * @return 升级单
     */
    public EscalationTicket createTicket(EscalationContext context) {
        String ticketId = UUID.randomUUID().toString();

        EscalationTicket ticket = EscalationTicket.builder()
                .ticketId(ticketId)
                .userId(context.getUserId())
                .sessionId(context.getSessionId())
                .question(context.getQuestion())
                .originalError(context.getErrorMessage())
                .priority(context.getPriority())
                .status(TicketStatus.PENDING)
                .createdAt(Instant.now())
                .context(context.getExtraData())
                .build();

        pendingTickets.put(ticketId, ticket);

        // 发送通知
        notifier.notifyNewTicket(ticket);

        log.info("创建升级单, ticketId={}, userId={}, priority={}",
                ticketId, context.getUserId(), context.getPriority());

        return ticket;
    }

    /**
     * 处理升级单
     *
     * @param ticketId   升级单 ID
     * @param resolution 处理结果
     * @param handlerId  处理人 ID
     */
    public void resolveTicket(String ticketId, String resolution, String handlerId) {
        EscalationTicket ticket = pendingTickets.remove(ticketId);
        if (ticket == null) {
            log.warn("升级单不存在: {}", ticketId);
            return;
        }

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolution(resolution);
        ticket.setHandlerId(handlerId);
        ticket.setResolvedAt(Instant.now());

        processedTickets.put(ticketId, ticket);

        log.info("升级单已处理, ticketId={}, handler={}", ticketId, handlerId);
    }

    /**
     * 获取待处理升级单
     */
    public List<EscalationTicket> getPendingTickets() {
        return List.copyOf(pendingTickets.values());
    }

    /**
     * 获取用户的待处理升级单
     */
    public List<EscalationTicket> getPendingTicketsByUser(String userId) {
        return pendingTickets.values().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取升级单
     */
    public EscalationTicket getTicket(String ticketId) {
        EscalationTicket ticket = pendingTickets.get(ticketId);
        if (ticket != null) return ticket;
        return processedTickets.get(ticketId);
    }

    /**
     * 设置升级通知器
     */
    public void setNotifier(EscalationNotifier notifier) {
        this.notifier = notifier;
    }

    /**
     * 升级单
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscalationTicket {
        private String ticketId;
        private String userId;
        private String sessionId;
        private String question;
        private String originalError;
        private Priority priority;
        private TicketStatus status;
        private Instant createdAt;
        private Instant resolvedAt;
        private String handlerId;
        private String resolution;
        private Map<String, Object> context;
    }

    /**
     * 升级上下文
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscalationContext {
        private String userId;
        private String sessionId;
        private String question;
        private String errorMessage;
        private Priority priority = Priority.NORMAL;
        private Map<String, Object> extraData;

        public static EscalationContext create(String userId, String sessionId, String question, String error) {
            EscalationContext ctx = new EscalationContext();
            ctx.userId = userId;
            ctx.sessionId = sessionId;
            ctx.question = question;
            ctx.errorMessage = error;
            return ctx;
        }
    }

    /**
     * 优先级
     */
    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    /**
     * 处理状态
     */
    public enum TicketStatus {
        PENDING, IN_PROGRESS, RESOLVED, CANCELLED
    }

    /**
     * 升级通知器接口
     */
    @FunctionalInterface
    public interface EscalationNotifier {
        void notifyNewTicket(EscalationTicket ticket);
    }
}
