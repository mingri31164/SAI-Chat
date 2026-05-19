import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import {
  PromptVersion,
  PromptVariant,
  TuningResult,
  TuningIteration,
  VariantResult_Prompt,
  TuningStrategy,
} from '@/types/prompt';
import type { EvalCase } from '@/types/observe';

/**
 * Create a prompt version
 */
export async function createPromptVersion(
  promptId: string,
  content: string,
  description?: string
): Promise<ApiResponse<PromptVersion>> {
  const res = await request.post<ApiResponse<PromptVersion>>('/api/prompt/version', null, {
    params: { promptId, content, description },
  });
  return res.data;
}

/**
 * Optimize prompt against test cases
 */
export async function optimizePrompt(
  promptId: string,
  content: string,
  testCases: EvalCase[],
  strategy: TuningStrategy = TuningStrategy.HYBRID
): Promise<ApiResponse<TuningResult>> {
  const res = await request.post<ApiResponse<TuningResult>>(
    '/api/prompt/optimize',
    testCases,
    { params: { promptId, content, strategy } }
  );
  return res.data;
}

/**
 * Generate N prompt variants
 */
export async function generatePromptVariants(
  content: string,
  strategy: TuningStrategy = TuningStrategy.HYBRID,
  count = 3
): Promise<ApiResponse<PromptVariant[]>> {
  const res = await request.post<ApiResponse<PromptVariant[]>>(
    '/api/prompt/variants',
    null,
    { params: { content, strategy, count } }
  );
  return res.data;
}

/**
 * Batch test multiple variants against test cases
 */
export async function batchTestVariants(
  promptId: string,
  variants: PromptVariant[],
  testCases: EvalCase[]
): Promise<ApiResponse<VariantResult_Prompt[]>> {
  const res = await request.post<ApiResponse<VariantResult_Prompt[]>>(
    '/api/prompt/batch-test',
    { variants, testCases },
    { params: { promptId } }
  );
  return res.data;
}

/**
 * Get best-scoring version for a prompt
 */
export async function getBestVersion(
  promptId: string
): Promise<ApiResponse<PromptVersion>> {
  const res = await request.get<ApiResponse<PromptVersion>>(`/api/prompt/best/${promptId}`);
  return res.data;
}

/**
 * Get version history for a prompt
 */
export async function getVersionHistory(
  promptId: string
): Promise<ApiResponse<PromptVersion[]>> {
  const res = await request.get<ApiResponse<PromptVersion[]>>(
    `/api/prompt/history/${promptId}`
  );
  return res.data;
}

/**
 * Get tuning iterations for a version
 */
export async function getTuningHistory(
  versionId: string
): Promise<ApiResponse<TuningIteration[]>> {
  const res = await request.get<ApiResponse<TuningIteration[]>>(
    `/api/prompt/tuning-history/${versionId}`
  );
  return res.data;
}

/**
 * Roll back to previous version
 */
export async function rollbackVersion(
  versionId: string
): Promise<ApiResponse<PromptVersion>> {
  const res = await request.post<ApiResponse<PromptVersion>>(
    `/api/prompt/rollback/${versionId}`
  );
  return res.data;
}

/**
 * Publish a version
 */
export async function publishVersion(
  versionId: string
): Promise<ApiResponse<PromptVersion>> {
  const res = await request.post<ApiResponse<PromptVersion>>(
    `/api/prompt/publish/${versionId}`
  );
  return res.data;
}
