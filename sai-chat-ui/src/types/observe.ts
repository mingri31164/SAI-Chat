// ============================================================
// Observability Types
// ============================================================
export interface MetricsSnapshot {
  timestamp: number;
  totalRequests: number;
  successRequests: number;
  failedRequests: number;
  timeoutRequests: number;
  successRate: number;
  errorRate: number;
  timeoutRate: number;
  averageDurationMs: number;
  p50LatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
  qps: number;
  llmCalls: number;
  averageLlmDurationMs: number;
  toolCalls: number;
  averageToolDurationMs: number;
  totalTokens: number;
  averageTokensPerRequest: number;
  totalCostYuan: number;
  activeSessions: number;
}

export interface ToolMetric {
  toolId: string;
  totalCalls: number;
  successCalls: number;
  failedCalls: number;
  successRate: number;
  minDurationMs: number;
  maxDurationMs: number;
  avgDurationMs: number;
}

export interface SessionMetrics {
  sessionId: string;
  requestCount: number;
  successCount: number;
  failedCount: number;
  timeoutCount: number;
  totalDurationMs: number;
  totalTokens: number;
  firstRequestTime: number;
  lastRequestTime: number;
}

// ============================================================
// Trace Types
// ============================================================
export enum TraceStatus {
  SUCCESS = 'SUCCESS',
  RUNNING = 'RUNNING',
  FAILED = 'FAILED',
  TIMEOUT = 'TIMEOUT',
  MAX_ITERATIONS_EXCEEDED = 'MAX_ITERATIONS_EXCEEDED',
  CANCELLED = 'CANCELLED',
  UNKNOWN = 'UNKNOWN',
}

export enum SpanType {
  LLM_REASONING = 'LLM_REASONING',
  TOOL_CALL = 'TOOL_CALL',
  REFLECTION = 'REFLECTION',
  MEMORY_RETRIEVAL = 'MEMORY_RETRIEVAL',
  CONTEXT_BUILDING = 'CONTEXT_BUILDING',
  RESPONSE_GENERATION = 'RESPONSE_GENERATION',
  INTENT_CLASSIFICATION = 'INTENT_CLASSIFICATION',
  QUERY_REWRITE = 'QUERY_REWRITE',
  RETRIEVAL = 'RETRIEVAL',
  RERANK = 'RERANK',
  OTHER = 'OTHER',
}

export enum SpanStatus {
  STARTED = 'STARTED',
  RUNNING = 'RUNNING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  TIMEOUT = 'TIMEOUT',
}

export enum TraceEventType {
  AGENT_STARTED = 'AGENT_STARTED',
  AGENT_COMPLETED = 'AGENT_COMPLETED',
  AGENT_FAILED = 'AGENT_FAILED',
  ITERATION_STARTED = 'ITERATION_STARTED',
  ITERATION_COMPLETED = 'ITERATION_COMPLETED',
  THINKING_STARTED = 'THINKING_STARTED',
  THINKING_COMPLETED = 'THINKING_COMPLETED',
  TOOL_CALL_STARTED = 'TOOL_CALL_STARTED',
  TOOL_CALL_COMPLETED = 'TOOL_CALL_COMPLETED',
  REFLECTION_STARTED = 'REFLECTION_STARTED',
  REFLECTION_COMPLETED = 'REFLECTION_COMPLETED',
  STATUS_CHANGED = 'STATUS_CHANGED',
  ERROR_OCCURRED = 'ERROR_OCCURRED',
  USER_INTERACTION = 'USER_INTERACTION',
  SYSTEM = 'SYSTEM',
}

export interface Span {
  spanId: string;
  parentSpanId?: string;
  traceId: string;
  name: string;
  type: SpanType;
  startTimeMs: number;
  endTimeMs?: number;
  durationMs?: number;
  status: SpanStatus;
  input?: string;
  output?: string;
  error?: string;
  attributes?: Record<string, string>;
  children?: Span[];
}

export interface TraceEvent {
  eventType: TraceEventType;
  timestamp: number;
  message: string;
  attributes?: Record<string, string>;
}

export interface AgentTrace {
  traceId: string;
  sessionId: string;
  userId: string;
  startTimeMs: number;
  endTimeMs?: number;
  totalDurationMs?: number;
  status: TraceStatus;
  spanList: Span[];
  events: TraceEvent[];
  metadata?: Record<string, unknown>;
  errorMessage?: string;
  errorStack?: string;
}

export interface TraceStatistics {
  totalTraces: number;
  successCount: number;
  failedCount: number;
  timeoutCount: number;
  averageDurationMs: number;
  totalTokens: number;
}

// ============================================================
// Evaluation Types
// ============================================================
export enum DifficultyLevel {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
  EXPERT = 'EXPERT',
}

export interface EvalCase {
  id: string;
  name: string;
  description?: string;
  input: string;
  expectedKeywords?: string[];
  expectedAnswer?: string;
  expectedPattern?: string;
  rejectPattern?: string;
  expectedToolCalls?: string[];
  maxIterations?: number;
  maxDurationMs?: number;
  tags?: string[];
  metadata?: Record<string, unknown>;
  difficulty: DifficultyLevel;
}

export interface EvalResult {
  caseId: string;
  actualAnswer: string;
  passed: boolean;
  overallScore: number;
  dimensionScores: Record<string, number>;
  durationMs: number;
  tokensUsed: number;
  toolCalls?: string[];
  errorMessage?: string;
}

export interface EvalReport {
  reportId: string;
  totalCases: number;
  passedCases: number;
  passRate: number;
  overallScore: number;
  averageDurationMs: number;
  averageIterations: number;
  dimensionScores: Record<string, number>;
  tagStatistics?: Record<string, { passRate: number; avgScore: number }>;
  failedResults: EvalResult[];
  performanceStats?: PerformanceStats;
}

export interface PerformanceStats {
  minDurationMs: number;
  maxDurationMs: number;
  avgDurationMs: number;
  p50DurationMs: number;
  p95DurationMs: number;
  p99DurationMs: number;
  totalTokens: number;
  totalCostYuan: number;
  totalToolCalls: number;
}

export interface Evaluation {
  score: number;
  details: string;
  matchedKeywords?: string[];
  unmatchedKeywords?: string[];
}
