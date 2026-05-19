import { create } from 'zustand';
import type {
  AgentStatus,
  ChatMessage,
  ToolCallRecord,
} from '@/types/agent';

export type ChatMode = 'RAG' | 'Agent';

export interface StreamEvent {
  type: string;
  data: unknown;
  timestamp: number;
}

export interface ChatSession {
  sessionId: string;
  messages: ChatMessage[];
  streamEvents: StreamEvent[];
  agentStatus: AgentStatus;
  toolCalls: ToolCallRecord[];
  isStreaming: boolean;
  currentStage?: 'intent' | 'rewrite' | 'retrieval' | 'thinking' | 'answer' | 'done' | 'error' | 'executing' | 'observing';
  retrievedChunks?: unknown[];
  intentScores?: unknown[];
  rewriteResult?: { rewrittenQuestion: string; subQuestions: string[] };
  stats?: {
    durationMs: number;
    totalTokens: number;
    totalCost: number;
    totalIterations: number;
  };
}

interface ChatStore {
  sessions: Record<string, ChatSession>;
  currentSessionId: string | null;
  chatMode: ChatMode;
  deepThinking: boolean;
  maxIterations: number;
  allowedTools: string[];
  excludedTools: string[];

  // Session management
  createSession: (sessionId?: string) => ChatSession;
  setCurrentSession: (sessionId: string) => void;
  deleteSession: (sessionId: string) => void;
  clearSession: (sessionId: string) => void;

  // Message management
  addMessage: (sessionId: string, message: ChatMessage) => void;
  appendToLastMessage: (sessionId: string, content: string) => void;

  // Stream event management
  addStreamEvent: (sessionId: string, event: StreamEvent) => void;
  updateStreamEvent: (sessionId: string, eventType: string, data: unknown) => void;
  setStreamEvents: (sessionId: string, events: StreamEvent[]) => void;

  // Status & tool tracking
  setAgentStatus: (sessionId: string, status: AgentStatus) => void;
  addToolCall: (sessionId: string, record: ToolCallRecord) => void;
  updateToolCall: (sessionId: string, step: number, updates: Partial<ToolCallRecord>) => void;
  setStreaming: (sessionId: string, streaming: boolean) => void;
  setCurrentStage: (sessionId: string, stage: ChatSession['currentStage']) => void;
  setRetrievedChunks: (sessionId: string, chunks: unknown[]) => void;
  setIntentScores: (sessionId: string, scores: unknown[]) => void;
  setRewriteResult: (sessionId: string, result: { rewrittenQuestion: string; subQuestions: string[] }) => void;
  setStats: (sessionId: string, stats: ChatSession['stats']) => void;

  // Settings
  setChatMode: (mode: ChatMode) => void;
  setDeepThinking: (enabled: boolean) => void;
  setMaxIterations: (max: number) => void;
  setAllowedTools: (tools: string[]) => void;
  setExcludedTools: (tools: string[]) => void;
}

const generateId = () => `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;

export const useChatStore = create<ChatStore>((set) => ({
  sessions: {},
  currentSessionId: null,
  chatMode: 'RAG',
  deepThinking: false,
  maxIterations: 10,
  allowedTools: [],
  excludedTools: [],

  createSession: (sessionId) => {
    const id = sessionId || generateId();
    const session: ChatSession = {
      sessionId: id,
      messages: [],
      streamEvents: [],
      agentStatus: 'IDLE' as AgentStatus,
      toolCalls: [],
      isStreaming: false,
    };
    set((state) => ({
      sessions: { ...state.sessions, [id]: session },
      currentSessionId: id,
    }));
    return session;
  },

  setCurrentSession: (sessionId) => set({ currentSessionId: sessionId }),

  deleteSession: (sessionId) => {
    set((state) => {
      const sessions = { ...state.sessions };
      delete sessions[sessionId];
      return {
        sessions,
        currentSessionId:
          state.currentSessionId === sessionId
            ? Object.keys(sessions)[0] || null
            : state.currentSessionId,
      };
    });
  },

  clearSession: (sessionId) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: {
          ...state.sessions[sessionId],
          messages: [],
          streamEvents: [],
          toolCalls: [],
          isStreaming: false,
          currentStage: undefined,
          retrievedChunks: undefined,
          intentScores: undefined,
          rewriteResult: undefined,
          stats: undefined,
        },
      },
    }));
  },

  addMessage: (sessionId, message) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: {
          ...state.sessions[sessionId],
          messages: [...(state.sessions[sessionId]?.messages || []), message],
        },
      },
    }));
  },

  appendToLastMessage: (sessionId, content) => {
    set((state) => {
      const session = state.sessions[sessionId];
      if (!session || session.messages.length === 0) return state;
      const messages = [...session.messages];
      const last = messages[messages.length - 1];
      if (last.role === 'assistant') {
        messages[messages.length - 1] = { ...last, content: last.content + content };
      } else {
        messages.push({ role: 'assistant', content, timestamp: Date.now() });
      }
      return {
        sessions: { ...state.sessions, [sessionId]: { ...session, messages } },
      };
    });
  },

  addStreamEvent: (sessionId, event) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: {
          ...state.sessions[sessionId],
          streamEvents: [...(state.sessions[sessionId]?.streamEvents || []), event],
        },
      },
    }));
  },

  updateStreamEvent: (sessionId, eventType, data) => {
    set((state) => {
      const session = state.sessions[sessionId];
      if (!session) return state;
      const events = [...session.streamEvents];
      const idx = events.findIndex((e) => e.type === eventType);
      if (idx >= 0) {
        events[idx] = { ...events[idx], data };
      } else {
        events.push({ type: eventType, data, timestamp: Date.now() });
      }
      return { sessions: { ...state.sessions, [sessionId]: { ...session, streamEvents: events } } };
    });
  },

  setStreamEvents: (sessionId, events) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), streamEvents: events },
      },
    }));
  },

  setAgentStatus: (sessionId, status) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), agentStatus: status },
      },
    }));
  },

  addToolCall: (sessionId, record) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: {
          ...(state.sessions[sessionId]),
          toolCalls: [...(state.sessions[sessionId]?.toolCalls || []), record],
        },
      },
    }));
  },

  updateToolCall: (sessionId, step, updates) => {
    set((state) => {
      const session = state.sessions[sessionId];
      if (!session) return state;
      const toolCalls = session.toolCalls.map((tc) =>
        tc.step === step ? { ...tc, ...updates } : tc
      );
      return { sessions: { ...state.sessions, [sessionId]: { ...session, toolCalls } } };
    });
  },

  setStreaming: (sessionId, streaming) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), isStreaming: streaming },
      },
    }));
  },

  setCurrentStage: (sessionId, stage) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), currentStage: stage },
      },
    }));
  },

  setRetrievedChunks: (sessionId, chunks) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), retrievedChunks: chunks },
      },
    }));
  },

  setIntentScores: (sessionId, scores) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), intentScores: scores },
      },
    }));
  },

  setRewriteResult: (sessionId, result) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), rewriteResult: result },
      },
    }));
  },

  setStats: (sessionId, stats) => {
    set((state) => ({
      sessions: {
        ...state.sessions,
        [sessionId]: { ...(state.sessions[sessionId]), stats },
      },
    }));
  },

  setChatMode: (mode) => set({ chatMode: mode }),
  setDeepThinking: (enabled) => set({ deepThinking: enabled }),
  setMaxIterations: (max) => set({ maxIterations: max }),
  setAllowedTools: (tools) => set({ allowedTools: tools }),
  setExcludedTools: (tools) => set({ excludedTools: tools }),
}));
