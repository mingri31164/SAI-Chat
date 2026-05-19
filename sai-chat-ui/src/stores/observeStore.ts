import { create } from 'zustand';
import type { MetricsSnapshot, AgentTrace } from '@/types/observe';

interface ObserveState {
  metricsSnapshot: MetricsSnapshot | null;
  recentTraces: AgentTrace[];
  selectedTrace: AgentTrace | null;
  isLoading: boolean;
  error: string | null;
  lastRefresh: number;

  setMetrics: (snapshot: MetricsSnapshot) => void;
  setTraces: (traces: AgentTrace[]) => void;
  setSelectedTrace: (trace: AgentTrace | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setLastRefresh: (ts: number) => void;
}

export const useObserveStore = create<ObserveState>((set) => ({
  metricsSnapshot: null,
  recentTraces: [],
  selectedTrace: null,
  isLoading: false,
  error: null,
  lastRefresh: 0,

  setMetrics: (snapshot) => set({ metricsSnapshot: snapshot, lastRefresh: Date.now() }),
  setTraces: (traces) => set({ recentTraces: traces }),
  setSelectedTrace: (trace) => set({ selectedTrace: trace }),
  setLoading: (loading) => set({ isLoading: loading }),
  setError: (error) => set({ error }),
  setLastRefresh: (ts) => set({ lastRefresh: ts }),
}));
