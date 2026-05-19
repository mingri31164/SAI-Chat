import { useCallback } from 'react';
import { useChatStore } from '@/stores/chatStore';

export function useConversation() {
  const {
    sessions,
    currentSessionId,
    chatMode,
    deepThinking,
    maxIterations,
    allowedTools,
    excludedTools,
    createSession,
    setCurrentSession,
    deleteSession,
    clearSession,
    addMessage,
    setChatMode,
    setDeepThinking,
    setMaxIterations,
    setAllowedTools,
    setExcludedTools,
  } = useChatStore();

  const currentSession = currentSessionId ? sessions[currentSessionId] : null;

  const sendMessage = useCallback(
    (
      question: string,
      startRAGStream: (question: string, userId: string, deepThinking: boolean) => void,
      startAgentStream: (question: string, userId: string, deepThinking: boolean, maxIterations: number) => void
    ) => {
      if (!currentSessionId) {
        createSession();
      }
      const sid = currentSessionId || Object.keys(sessions)[Object.keys(sessions).length - 1] || '';
      if (!sid) return;

      // Add user message
      addMessage(sid, {
        role: 'user',
        content: question,
        timestamp: Date.now(),
      });

      const userId = localStorage.getItem('satoken')?.slice(-20) || 'guest';

      if (chatMode === 'RAG') {
        startRAGStream(question, userId, deepThinking);
      } else {
        startAgentStream(question, userId, deepThinking, maxIterations);
      }
    },
    [currentSessionId, sessions, chatMode, deepThinking, maxIterations, createSession, addMessage]
  );

  return {
    sessions,
    currentSession,
    currentSessionId,
    chatMode,
    deepThinking,
    maxIterations,
    allowedTools,
    excludedTools,
    createSession,
    setCurrentSession,
    deleteSession,
    clearSession,
    sendMessage,
    setChatMode,
    setDeepThinking,
    setMaxIterations,
    setAllowedTools,
    setExcludedTools,
  };
}
