import { useCallback, useEffect, useRef } from 'react';
import { useSSE } from './useSSE';
import { useChatStore } from '@/stores/chatStore';
import type {
  SSEIntentEvent,
  SSERewriteEvent,
  SSERetrievalEvent,
  SSEToolStartEvent,
  SSEToolEndEvent,
  SSEThinkingEvent,
  SSEAnswerEvent,
  SSEDoneEvent,
  SSEErrorEvent,
  SSEIntentEvent_Agent,
  SSEDoneEvent_Agent,
} from '@/types/agent';
import { AgentStatus } from '@/types/agent';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export function useRAGStream(sessionId: string) {
  const { state, connect, disconnect, on } = useSSE();
  const appendRef = useRef<string>('');

  const {
    addMessage,
    appendToLastMessage,
    setCurrentStage,
    setRetrievedChunks,
    setIntentScores,
    setRewriteResult,
    setStreaming,
    setStats,
    addStreamEvent,
  } = useChatStore();

  // Register event handlers once
  useEffect(() => {
    const unsubs: Array<() => void> = [];

    unsubs.push(
      on('intent', (data) => {
        const ev = data as SSEIntentEvent;
        setCurrentStage(sessionId, 'intent');
        setIntentScores(sessionId, ev.scores);
        addStreamEvent(sessionId, { type: 'intent', data: ev, timestamp: Date.now() });
      })
    );

    unsubs.push(
      on('rewrite', (data) => {
        const ev = data as SSERewriteEvent;
        setCurrentStage(sessionId, 'rewrite');
        setRewriteResult(sessionId, ev);
        addStreamEvent(sessionId, { type: 'rewrite', data: ev, timestamp: Date.now() });
      })
    );

    unsubs.push(
      on('retrieval', (data) => {
        const ev = data as SSERetrievalEvent;
        setCurrentStage(sessionId, 'retrieval');
        setRetrievedChunks(sessionId, ev.chunks);
        addStreamEvent(sessionId, { type: 'retrieval', data: ev, timestamp: Date.now() });
      })
    );

    unsubs.push(
      on('thinking', (data) => {
        setCurrentStage(sessionId, 'thinking');
        // thinking events may be incremental
        const ev = data as SSEThinkingEvent;
        addStreamEvent(sessionId, { type: 'thinking', data: ev, timestamp: Date.now() });
      })
    );

    unsubs.push(
      on('answer', (data) => {
        setCurrentStage(sessionId, 'answer');
        const text = typeof data === 'string' ? data : (data as SSEAnswerEvent).content;
        if (appendRef.current === '') {
          // First answer chunk: add new message
          addMessage(sessionId, {
            role: 'assistant',
            content: text,
            timestamp: Date.now(),
          });
          appendRef.current = text;
        } else {
          // Subsequent chunks: append to last
          appendToLastMessage(sessionId, text);
          appendRef.current += text;
        }
      })
    );

    unsubs.push(
      on('done', (data) => {
        const ev = data as SSEDoneEvent;
        setCurrentStage(sessionId, 'done');
        setStreaming(sessionId, false);
        setStats(sessionId, {
          durationMs: ev.durationMs,
          totalTokens: ev.totalTokens,
          totalCost: 0,
          totalIterations: 0,
        });
        addStreamEvent(sessionId, { type: 'done', data: ev, timestamp: Date.now() });
        appendRef.current = '';
      })
    );

    unsubs.push(
      on('error', (data) => {
        const ev = data as SSEErrorEvent;
        setCurrentStage(sessionId, 'error');
        setStreaming(sessionId, false);
        addMessage(sessionId, {
          role: 'assistant',
          content: `发生错误: ${ev.error || '未知错误'}`,
          timestamp: Date.now(),
        });
        appendRef.current = '';
      })
    );

    return () => {
      unsubs.forEach((unsub) => unsub());
    };
  }, [sessionId, on, addMessage, appendToLastMessage, setCurrentStage, setRetrievedChunks, setIntentScores, setRewriteResult, setStreaming, setStats, addStreamEvent]);

  const startStream = useCallback(
    (question: string, userId: string, deepThinking = false) => {
      appendRef.current = '';
      setStreaming(sessionId, true);
      const params: Record<string, string> = {
        question,
        sessionId,
        userId,
        deepThinking: String(deepThinking),
      };
      connect(`${BASE_URL}/api/rag/chat/sse`, params);
    },
    [sessionId, connect, setStreaming]
  );

  return {
    status: state.status,
    error: state.error,
    startStream,
    stopStream: disconnect,
  };
}

export function useAgentStream(sessionId: string) {
  const { state, connect, disconnect, on } = useSSE();
  const appendRef = useRef<string>('');

  const {
    addMessage,
    appendToLastMessage,
    appendReasoningToLast,
    setAgentStatus,
    addToolCall,
    updateToolCall,
    setCurrentStage,
    setStreaming,
    setStats,
    addStreamEvent,
  } = useChatStore();

  useEffect(() => {
    const unsubs: Array<() => void> = [];

    unsubs.push(
      on('intent', (data) => {
        const ev = data as SSEIntentEvent_Agent;
        setAgentStatus(sessionId, AgentStatus.THINKING);
        addStreamEvent(sessionId, { type: 'intent', data: ev, timestamp: Date.now() });
      })
    );

    // 思考事件 → 直接追加到消息气泡的 reasoning 字段
    unsubs.push(
      on('thinking', (data) => {
        setCurrentStage(sessionId, 'thinking');
        const ev = data as SSEThinkingEvent;
        if (appendRef.current === '') {
          addMessage(sessionId, {
            role: 'assistant',
            content: '',
            timestamp: Date.now(),
            reasoning: ev.thought,
          });
          appendRef.current = '@reasoning';
        } else if (appendRef.current === '@reasoning') {
          appendReasoningToLast(sessionId, ev.thought);
        }
        addStreamEvent(sessionId, { type: 'thinking', data: ev, timestamp: Date.now() });
      })
    );

    // reasoning 原始 token 事件 → 同样追加到 reasoning 字段
    unsubs.push(
      on('reasoning', (data) => {
        setCurrentStage(sessionId, 'thinking');
        const ev = data as { content: string };
        if (appendRef.current === '') {
          addMessage(sessionId, {
            role: 'assistant',
            content: '',
            timestamp: Date.now(),
            reasoning: ev.content,
          });
          appendRef.current = '@reasoning';
        } else if (appendRef.current === '@reasoning') {
          appendReasoningToLast(sessionId, ev.content);
        }
        addStreamEvent(sessionId, { type: 'reasoning', data: ev, timestamp: Date.now() });
      })
    );

    // 工具开始 → 不再输出 thinking，进入执行阶段
    unsubs.push(
      on('tool_start', (data) => {
        setAgentStatus(sessionId, AgentStatus.EXECUTING);
        setCurrentStage(sessionId, 'executing');
        const ev = data as SSEToolStartEvent;
        addToolCall(sessionId, {
          step: ev.step,
          toolId: ev.toolId,
          toolName: ev.toolName,
          parameters: {},
          startTime: Date.now(),
        });
        addStreamEvent(sessionId, { type: 'tool_start', data: ev, timestamp: Date.now() });
      })
    );

    unsubs.push(
      on('tool_end', (data) => {
        setAgentStatus(sessionId, AgentStatus.OBSERVING);
        setCurrentStage(sessionId, 'observing');
        const ev = data as SSEToolEndEvent;
        updateToolCall(sessionId, ev.step, {
          endTime: Date.now(),
          durationMs: Date.now(),
          success: ev.success,
          result: ev.summary,
        });
        addStreamEvent(sessionId, { type: 'tool_end', data: ev, timestamp: Date.now() });
      })
    );

    // 回答事件 → 覆盖到 content 字段，thinking 内容保留在 reasoning 字段
    unsubs.push(
      on('answer', (data) => {
        setCurrentStage(sessionId, 'answer');
        const ev = data as SSEAnswerEvent;
        const text = ev?.content ?? '';
        if (appendRef.current === '' || appendRef.current === '@reasoning') {
          addMessage(sessionId, { role: 'assistant', content: text, timestamp: Date.now() });
          appendRef.current = text;
        } else {
          appendToLastMessage(sessionId, text);
          appendRef.current += text;
        }
      })
    );

    unsubs.push(
      on('done', (data) => {
        const ev = data as SSEDoneEvent_Agent;
        setAgentStatus(sessionId, ev.status as AgentStatus);
        setCurrentStage(sessionId, 'done');
        setStreaming(sessionId, false);
        setStats(sessionId, {
          durationMs: ev.durationMs ?? 0,
          totalTokens: ev.totalTokens,
          totalCost: 0,
          totalIterations: ev.totalSteps,
        });
        addStreamEvent(sessionId, { type: 'done', data: ev, timestamp: Date.now() });
        appendRef.current = '';
      })
    );

    unsubs.push(
      on('error', (data) => {
        const ev = data as SSEErrorEvent;
        setAgentStatus(sessionId, AgentStatus.FAILED);
        setCurrentStage(sessionId, 'error');
        setStreaming(sessionId, false);
        addMessage(sessionId, {
          role: 'assistant',
          content: `发生错误: ${ev.error || '未知错误'}`,
          timestamp: Date.now(),
        });
        appendRef.current = '';
      })
    );

    return () => {
      unsubs.forEach((unsub) => unsub());
    };
  }, [sessionId, on, addMessage, appendToLastMessage, appendReasoningToLast, setAgentStatus, addToolCall, updateToolCall, setCurrentStage, setStreaming, setStats, addStreamEvent]);

  const startStream = useCallback(
    (question: string, userId: string, deepThinking = false, maxIterations = 10) => {
      appendRef.current = '';
      setStreaming(sessionId, true);
      const params: Record<string, string | number> = {
        question,
        sessionId,
        userId,
        deepThinking: deepThinking ? 1 : 0,
        maxIterations,
      };
      connect(`${BASE_URL}/agent/chat/stream`, params);
    },
    [sessionId, connect, setStreaming]
  );

  return {
    status: state.status,
    error: state.error,
    startStream,
    stopStream: disconnect,
  };
}
