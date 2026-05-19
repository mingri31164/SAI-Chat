// ============================================================
// Unified API Response Wrapper (matches backend Result<T>)
// ============================================================
export interface ApiResponse<T = unknown> {
  code: string;
  message: string;
  data: T;
  requestId?: string;
}

// ============================================================
// Agent Types
// ============================================================
export enum AgentStatus {
  IDLE = 'IDLE',
  THINKING = 'THINKING',
  PLANNING = 'PLANNING',
  EXECUTING = 'EXECUTING',
  OBSERVING = 'OBSERVING',
  REFLECTING = 'REFLECTING',
  WAITING = 'WAITING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  EXCEEDED = 'EXCEEDED',
}

export enum AgentMode {
  REACT = 'REACT',
  PLAN_AND_EXECUTE = 'PLAN_AND_EXECUTE',
  HYBRID = 'HYBRID',
}

export interface AgentChatRequest {
  sessionId?: string;
  userId?: string;
  question: string;
  deepThinking?: boolean;
  maxIterations?: number;
  maxTokens?: number;
  maxBudget?: number;
  allowedTools?: string[];
  excludedTools?: string[];
  mode?: AgentMode;
}

export interface ToolCallRecord {
  step: number;
  toolId: string;
  toolName: string;
  parameters: Record<string, unknown>;
  startTime: number;
  endTime?: number;
  durationMs?: number;
  success?: boolean;
  result?: string;
  error?: string;
}

export interface AgentResponse {
  success: boolean;
  answer: string;
  status: AgentStatus;
  durationMs: number;
  totalIterations: number;
  toolCallCount: number;
  totalTokens: number;
  totalCost: number;
  toolCallTrace: ToolCallRecord[];
  thoughtHistory: string[];
  errorMessage?: string;
}

// ============================================================
// SSE Event Payloads
// ============================================================
export interface SSEIntentEvent {
  scores: NodeScore[];
  topScore: number;
}

export interface SSERewriteEvent {
  rewrittenQuestion: string;
  subQuestions: string[];
}

export interface SSERetrievalEvent {
  count: number;
  chunks: RetrievedChunk[];
}

export interface SSEThinkingEvent {
  step: number;
  thought: string;
}

export interface SSEReasoningChunkEvent {
  content: string;
}

export interface SSEAnswerEvent {
  content: string;
}

export interface SSEDoneEvent {
  success: boolean;
  answer: string;
  totalSteps: number;
  totalTokens: number;
  durationMs: number;
}

export interface SSEErrorEvent {
  error: string;
  retryable: boolean;
}

export interface ToolStartEvent {
  step: number;
  toolId: string;
  toolName: string;
}

export interface ToolEndEvent {
  step: number;
  toolId: string;
  toolName: string;
  success: boolean;
  summary: string;
}

export interface SSEIntentEvent_Agent {
  status: string;
  message: string;
}

export interface SSEToolStartEvent {
  step: number;
  toolId: string;
  toolName: string;
}

export interface SSEToolEndEvent {
  step: number;
  toolId: string;
  toolName: string;
  success: boolean;
  summary: string;
}

export interface SSEDoneEvent_Agent {
  success: boolean;
  status: string;
  answer: string;
  totalSteps: number;
  totalTokens: number;
  durationMs?: number;
}

// ============================================================
// RAG Types
// ============================================================
export interface RetrievedChunk {
  id: string;
  text: string;
  score: number;
}

export interface RewriteResult {
  rewrittenQuestion: string;
  subQuestions: string[];
}

export interface NodeScore {
  id: string;
  path: string;
  name: string;
  score: number;
  kind?: IntentKind;
  description?: string;
}

export interface GuidanceDecision {
  isPrompt: boolean;
  prompt?: string;
  options?: string[];
}

// ============================================================
// Intent Tree Types
// ============================================================
export enum IntentKind {
  KB = 'KB',
  SYSTEM = 'SYSTEM',
  MCP = 'MCP',
}

export enum IntentLevel {
  DOMAIN = 'DOMAIN',
  CATEGORY = 'CATEGORY',
  TOPIC = 'TOPIC',
}

export interface IntentNode {
  id: string;
  name: string;
  level: IntentLevel;
  kind: IntentKind;
  parentId?: string;
  description?: string;
  mcpToolId?: string;
  collectionName?: string;
  examples?: string[];
  children?: IntentNode[];
}

// ============================================================
// MCP Types
// ============================================================
export interface MCPToolDefinition {
  toolId: string;
  name: string;
  description: string;
  parameters: Record<string, MCPToolParameter>;
  requireUserId?: boolean;
}

export interface MCPToolParameter {
  type: string;
  description?: string;
  required?: boolean;
  enumValues?: string[];
}

export interface MCPToolResponse {
  toolId: string;
  result: string;
  success: boolean;
  error?: string;
}

// ============================================================
// Memory Types
// ============================================================
export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: number;
}

export interface ConversationSummary {
  sessionId: string;
  summary: string;
  turnCount: number;
  createdAt: number;
}

export enum MemoryType {
  WORKING = 'WORKING',
  EPISODIC = 'EPISODIC',
  SEMANTIC = 'SEMANTIC',
}

export enum KnowledgeType {
  FACT = 'FACT',
  PATTERN = 'PATTERN',
  RULE = 'RULE',
  HEURISTIC = 'HEURISTIC',
  SUCCESS_CASE = 'SUCCESS_CASE',
  FAILURE_CASE = 'FAILURE_CASE',
  BEST_PRACTICE = 'BEST_PRACTICE',
}
