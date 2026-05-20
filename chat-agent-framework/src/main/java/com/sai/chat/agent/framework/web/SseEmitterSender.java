package com.sai.chat.agent.framework.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE（Server-Sent Events）发送器封装类
 *
 * <p>该类对 Spring 的 SseEmitter 进行封装，提供了线程安全的事件发送功能，
 * 统一处理连接关闭状态和异常情况。主要用于服务端向客户端推送实时数据流</p>
 */
@Slf4j
public class SseEmitterSender {

    private final SseEmitter emitter;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicInteger pendingSends = new AtomicInteger(0);

    public SseEmitterSender(SseEmitter emitter) {
        this.emitter = emitter;
    }

    public void sendEvent(String eventName, Object data) {
        if (closed.get()) {
            return;
        }
        pendingSends.incrementAndGet();
        try {
            String json = data instanceof String ? (String) data : new com.google.gson.Gson().toJson(data);
            log.debug("[SseEmitterSender] 发送事件: eventName={}, data={}", eventName, json);
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (Exception e) {
            fail(e);
        } finally {
            int remaining = pendingSends.decrementAndGet();
            if (closed.get() && remaining == 0) {
                emitter.complete();
            }
        }
    }

    public void complete() {
        closed.set(true);
        if (pendingSends.get() == 0) {
            emitter.complete();
        }
    }

    public void fail(Throwable throwable) {
        if (closed.compareAndSet(false, true)) {
            emitter.completeWithError(throwable);
        }
        log.warn("SSE send failed", throwable);
    }
}
