// ============================================================
// Prompt Tuning Types
// ============================================================
export enum TuningStrategy {
  GRAMMATICAL = 'GRAMMATICAL',
  STRUCTURAL = 'STRUCTURAL',
  CONCISENESS = 'CONCISENESS',
  EXAMPLES = 'EXAMPLES',
  HYBRID = 'HYBRID',
}

export enum VersionStatus {
  DRAFT = 'DRAFT',
  TESTING = 'TESTING',
  PUBLISHED = 'PUBLISHED',
  ROLLED_BACK = 'ROLLED_BACK',
  ARCHIVED = 'ARCHIVED',
}

export interface PromptVersion {
  versionId: string;
  promptId: string;
  content: string;
  description?: string;
  versionNumber: number;
  status: VersionStatus;
  createdTime: number;
  publishedTime?: number;
  metrics?: PromptMetrics;
}

export interface PromptVariant {
  variantId: string;
  content: string;
  changeType: string;
  score: number;
}

export interface PromptMetrics {
  evalScore: number;
  responseQuality: number;
  tokenCost: number;
  iterationCount: number;
  successRate: number;
}

export interface TuningIteration {
  iterationNumber: number;
  variants: PromptVariant[];
  bestVariant: PromptVariant;
  bestScore: number;
  improvement: number;
}

export interface TuningResult {
  promptId: string;
  initialContent: string;
  initialScore: number;
  finalContent: string;
  finalScore: number;
  totalImprovement: number;
  iterations: number;
  successful: boolean;
  startTime: number;
  endTime: number;
}

export interface VariantResult_Prompt {
  variantId: string;
  content: string;
  score: number;
  responseQuality: number;
  tokenCost: number;
}
