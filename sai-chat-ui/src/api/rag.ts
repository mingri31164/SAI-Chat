import request from '@/utils/request';
import type { ApiResponse, NodeScore, RewriteResult } from '@/types/agent';

/**
 * Classify user intent from a question
 */
export async function classifyIntent(
  question: string,
  topN = 3,
  minScore = 0.35
): Promise<ApiResponse<NodeScore[]>> {
  const res = await request.get<ApiResponse<NodeScore[]>>('/api/rag/intent/classify', {
    params: { question, topN, minScore },
  });
  return res.data;
}

/**
 * Rewrite and split a query using LLM
 */
export async function rewriteQuery(
  question: string,
  sessionId?: string
): Promise<ApiResponse<RewriteResult>> {
  const res = await request.post<ApiResponse<RewriteResult>>('/api/rag/query/rewrite', null, {
    params: { question, sessionId },
  });
  return res.data;
}

/**
 * Fast rule-based rewrite (no LLM call)
 */
export async function rewriteQueryRule(
  question: string
): Promise<ApiResponse<RewriteResult>> {
  const res = await request.post<ApiResponse<RewriteResult>>(
    '/api/rag/query/rewrite/rule',
    null,
    { params: { question } }
  );
  return res.data;
}

/**
 * SSE health check (for connection testing)
 */
export async function sseHealth(): Promise<ApiResponse<string>> {
  const res = await request.get<ApiResponse<string>>('/api/rag/sse/health');
  return res.data;
}
