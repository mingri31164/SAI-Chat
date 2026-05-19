// ============================================================
// A/B Testing Types
// ============================================================
export enum TestStatus {
  DRAFT = 'DRAFT',
  RUNNING = 'RUNNING',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface ABTest {
  testId: string;
  name: string;
  description?: string;
  status: TestStatus;
  variantCount: number;
  variantWeights: number[];
  minSignificance?: number;
  minSampleSize?: number;
  createdTime: number;
  startTime?: number;
  endTime?: number;
  winnerVariantId?: string;
}

export interface ABVariant {
  variantId: string;
  variantName: string;
  weight: number;
  isControl: boolean;
  config?: string;
}

export interface UserAssignment {
  testId: string;
  userId: string;
  variantId: string;
  assignedTime: number;
}

export interface TestResult {
  testId: string;
  testName: string;
  status: TestStatus;
  startTime: number;
  endTime?: number;
  variants: VariantResult[];
  winnerVariantId?: string;
  significance: number;
  recommendation: string;
}

export interface VariantResult {
  variantId: string;
  variantName: string;
  isControl: boolean;
  impressions: number;
  conversions: number;
  conversionRate: number;
  averageLatencyMs: number;
  errorRate: number;
  revenue: number;
  confidenceInterval: number;
}

export enum MetricType {
  IMPRESSION = 'IMPRESSION',
  CONVERSION = 'CONVERSION',
  REVENUE = 'REVENUE',
  LATENCY = 'LATENCY',
  ERROR = 'ERROR',
}

export interface MetricRecord {
  testId: string;
  variantId: string;
  metricType: MetricType;
  value: number;
  userId?: string;
  timestamp: number;
}
