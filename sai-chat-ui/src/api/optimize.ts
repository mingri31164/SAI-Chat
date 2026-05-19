import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import type {
  OptimizationSuggestion,
  OptimizationExperiment,
} from '@/types/document';

interface HealthStatus {
  status: 'HEALTHY' | 'WARNING' | 'DEGRADED' | 'CRITICAL';
  score: number;
  issues: string[];
  timestamp: number;
}

interface PerformanceReport {
  healthStatus: HealthStatus;
  analysis: Record<string, unknown>;
  suggestions: OptimizationSuggestion[];
  timestamp: number;
}

/**
 * Run performance analysis (default: last 1 hour)
 */
export async function runAnalysis(since?: number): Promise<ApiResponse<PerformanceReport>> {
  const res = await request.get<ApiResponse<PerformanceReport>>('/api/optimize/analyze', {
    params: since ? { since } : {},
  });
  return res.data;
}

/**
 * Analyze single trace performance
 */
export async function analyzeTracePerformance(
  traceId: string
): Promise<ApiResponse<Record<string, unknown>>> {
  const res = await request.get<ApiResponse<Record<string, unknown>>>(
    `/api/optimize/trace/${traceId}/analyze`
  );
  return res.data;
}

/**
 * Get health status summary
 */
export async function getHealthStatus(): Promise<ApiResponse<HealthStatus>> {
  const res = await request.get<ApiResponse<HealthStatus>>('/api/optimize/health');
  return res.data;
}

/**
 * Create optimization experiment from suggestion
 */
export async function createExperiment(
  suggestion: OptimizationSuggestion
): Promise<ApiResponse<OptimizationExperiment>> {
  const res = await request.post<ApiResponse<OptimizationExperiment>>(
    '/api/optimize/experiment',
    suggestion
  );
  return res.data;
}

/**
 * Start experiment with baseline metrics
 */
export async function startExperiment(experimentId: string): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(
    `/api/optimize/experiment/${experimentId}/start`
  );
  return res.data;
}

/**
 * Update experiment with current metrics
 */
export async function updateExperimentMetrics(
  experimentId: string,
  metrics: Record<string, number>
): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(
    `/api/optimize/experiment/${experimentId}/metrics`,
    metrics
  );
  return res.data;
}

/**
 * Complete experiment
 */
export async function completeExperiment(
  experimentId: string,
  success = true
): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(
    `/api/optimize/experiment/${experimentId}/complete`,
    null,
    { params: { success } }
  );
  return res.data;
}

/**
 * Roll back experiment
 */
export async function rollbackExperiment(
  experimentId: string
): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(
    `/api/optimize/experiment/${experimentId}/rollback`
  );
  return res.data;
}

/**
 * Gradual rollout (0-100%)
 */
export async function rolloutExperiment(
  experimentId: string,
  percentage: number
): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(
    `/api/optimize/experiment/${experimentId}/rollout`,
    null,
    { params: { percentage } }
  );
  return res.data;
}

/**
 * Get experiment details
 */
export async function getExperiment(
  experimentId: string
): Promise<ApiResponse<OptimizationExperiment>> {
  const res = await request.get<ApiResponse<OptimizationExperiment>>(
    `/api/optimize/experiment/${experimentId}`
  );
  return res.data;
}

/**
 * Get all experiments
 */
export async function getAllExperiments(): Promise<ApiResponse<OptimizationExperiment[]>> {
  const res = await request.get<ApiResponse<OptimizationExperiment[]>>('/api/optimize/experiments');
  return res.data;
}

/**
 * Get running experiments
 */
export async function getRunningExperiments(): Promise<ApiResponse<OptimizationExperiment[]>> {
  const res = await request.get<ApiResponse<OptimizationExperiment[]>>(
    '/api/optimize/experiments/running'
  );
  return res.data;
}

/**
 * Generate optimization summary report
 */
export async function getOptimizationReport(): Promise<
  ApiResponse<{
    totalExperiments: number;
    running: number;
    completed: number;
    averageImprovement: number;
    recentSuggestions: OptimizationSuggestion[];
  }>
> {
  const res = await request.get('/api/optimize/report');
  return res.data;
}
