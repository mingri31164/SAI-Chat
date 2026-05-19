// ============================================================
// Document & Knowledge Base Types
// ============================================================
export interface ChunkMetadata {
  chunkId: string;
  docId: string;
  kbId: string;
  docTitle: string;
  content: string;
  index: number;
  sectionPath?: string;
  score?: number;
}

export interface UploadProgress {
  stage: 'parsing' | 'chunking' | 'embedding' | 'indexing' | 'complete' | 'error';
  progress: number;
  message: string;
  chunkCount?: number;
  error?: string;
}

export interface DocumentUploadResponse {
  success: boolean;
  ragTag: string;
  fileCount: number;
  chunkCount: number;
  message?: string;
}

export interface TagInfo {
  tag: string;
  documentCount?: number;
  chunkCount?: number;
}

// ============================================================
// Review / Pattern Types
// ============================================================
export interface SuccessPattern {
  patternId: string;
  title: string;
  pattern: string;
  context: string;
  triggerCondition?: string;
  steps?: string[];
  effectivenessScore: number;
  usageCount: number;
  tags?: string[];
  accessCount: number;
  createdAt: number;
}

export interface FailureLesson {
  lessonId: string;
  title: string;
  lesson: string;
  rootCause: string;
  prevention?: string;
  symptoms?: string[];
  occurrenceCount: number;
  tags?: string[];
  createdAt: number;
}

export interface ReviewReport {
  traceId: string;
  sessionId: string;
  analysisTime: number;
  successFactors: string[];
  failureReasons: string[];
  keyDecisions: string[];
  overallScore: number;
  recommendations: string[];
}

export interface AggregatedReview {
  analyzedTraces: number;
  commonSuccessFactors: string[];
  commonFailureReasons: string[];
  topRecommendations: string[];
  patternDistribution?: Record<string, number>;
}

export interface ReviewStats {
  totalPatterns: number;
  totalLessons: number;
  avgPatternEffectiveness: number;
  topTags: string[];
}

// ============================================================
// Experiment Types
// ============================================================
export enum ExperimentStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  ROLLED_BACK = 'ROLLED_BACK',
}

export interface OptimizationSuggestion {
  type: string;
  description: string;
  targetMetric: string;
  currentValue: number;
  targetValue: number;
  confidence: number;
}

export interface OptimizationExperiment {
  experimentId: string;
  suggestion: OptimizationSuggestion;
  status: ExperimentStatus;
  baselineMetrics?: Record<string, number>;
  currentMetrics?: Record<string, number>;
  improvements?: Record<string, number>;
  rolloutPercentage: number;
  createdAt: number;
  startedAt?: number;
  completedAt?: number;
}

// ============================================================
// Permission & Security Types
// ============================================================
export interface PermissionContext {
  userId: string;
  permission: string;
  resourceId?: string;
}

export interface PermissionResult {
  allowed: boolean;
  reason?: string;
}

export interface ContentSafetyResult {
  allowed: boolean;
  riskScore?: number;
  violations?: SafetyViolation[];
}

export interface SafetyViolation {
  type: string;
  description: string;
  riskScore: number;
  matchedContent?: string;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER',
  VIEWER = 'VIEWER',
  GUEST = 'GUEST',
}

export interface User {
  userId: string;
  username: string;
  role: UserRole;
  token?: string;
}
