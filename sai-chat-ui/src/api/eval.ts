import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import type { EvalCase, EvalReport } from '@/types/observe';

interface EvalReportStats {
  total: number;
  byCategory: Record<string, number>;
  byDifficulty: Record<string, number>;
}

/**
 * Run all default evaluation cases
 */
export async function runAllEval(parallel = false): Promise<ApiResponse<EvalReport>> {
  const res = await request.post<ApiResponse<EvalReport>>('/api/eval/run', null, {
    params: { parallel },
  });
  return res.data;
}

/**
 * Run custom list of evaluation cases
 */
export async function runCustomEval(
  cases: EvalCase[]
): Promise<ApiResponse<EvalReport>> {
  const res = await request.post<ApiResponse<EvalReport>>('/api/eval/run/custom', cases);
  return res.data;
}

/**
 * Run quick smoke-test subset
 */
export async function runSmokeTest(): Promise<ApiResponse<EvalReport>> {
  const res = await request.post<ApiResponse<EvalReport>>('/api/eval/smoke-test');
  return res.data;
}

/**
 * Get evaluation case library stats
 */
export async function getEvalLibraryStats(): Promise<ApiResponse<EvalReportStats>> {
  const res = await request.get<ApiResponse<EvalReportStats>>('/api/eval/library/stats');
  return res.data;
}

/**
 * Get all evaluation cases
 */
export async function getEvalLibrary(): Promise<ApiResponse<EvalCase[]>> {
  const res = await request.get<ApiResponse<EvalCase[]>>('/api/eval/library');
  return res.data;
}

/**
 * Get simple Q&A evaluation cases
 */
export async function getSimpleQACases(): Promise<ApiResponse<EvalCase[]>> {
  const res = await request.get<ApiResponse<EvalCase[]>>('/api/eval/library/simple-qa');
  return res.data;
}

/**
 * Get tool-call evaluation cases
 */
export async function getToolCallCases(): Promise<ApiResponse<EvalCase[]>> {
  const res = await request.get<ApiResponse<EvalCase[]>>('/api/eval/library/tool-call');
  return res.data;
}

/**
 * Get reasoning evaluation cases
 */
export async function getReasoningCases(): Promise<ApiResponse<EvalCase[]>> {
  const res = await request.get<ApiResponse<EvalCase[]>>('/api/eval/library/reasoning');
  return res.data;
}

/**
 * Export a report as JSON
 */
export async function exportEvalReport(reportId: string): Promise<ApiResponse<EvalReport>> {
  const res = await request.get<ApiResponse<EvalReport>>(
    `/api/eval/report/${reportId}/export`
  );
  return res.data;
}
