import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import type {
  MetricsSnapshot,
  ToolMetric,
  SessionMetrics,
  AgentTrace,
  TraceStatistics,
} from '@/types/observe';

/**
 * Get global metrics snapshot
 */
export async function getMetricsSnapshot(): Promise<ApiResponse<MetricsSnapshot>> {
  const res = await request.get<ApiResponse<MetricsSnapshot>>('/api/observe/metrics/snapshot');
  return res.data;
}

/**
 * Get per-tool metrics
 */
export async function getToolMetrics(): Promise<ApiResponse<ToolMetric[]>> {
  const res = await request.get<ApiResponse<ToolMetric[]>>('/api/observe/metrics/tools');
  return res.data;
}

/**
 * Get session-scoped metrics
 */
export async function getSessionMetrics(
  sessionId: string
): Promise<ApiResponse<SessionMetrics>> {
  const res = await request.get<ApiResponse<SessionMetrics>>(
    `/api/observe/metrics/session/${sessionId}`
  );
  return res.data;
}

/**
 * Get full trace detail as JSON
 */
export async function getTraceDetail(traceId: string): Promise<ApiResponse<AgentTrace>> {
  const res = await request.get<ApiResponse<AgentTrace>>(`/api/observe/trace/${traceId}`);
  return res.data;
}

/**
 * Get all traces for a session
 */
export async function getSessionTraces(
  sessionId: string
): Promise<ApiResponse<AgentTrace[]>> {
  const res = await request.get<ApiResponse<AgentTrace[]>>(
    `/api/observe/trace/session/${sessionId}`
  );
  return res.data;
}

/**
 * Get N most recent traces
 */
export async function getRecentTraces(limit = 20): Promise<ApiResponse<AgentTrace[]>> {
  const res = await request.get<ApiResponse<AgentTrace[]>>('/api/observe/trace/recent', {
    params: { limit },
  });
  return res.data;
}

/**
 * Get all failed traces
 */
export async function getFailedTraces(): Promise<ApiResponse<AgentTrace[]>> {
  const res = await request.get<ApiResponse<AgentTrace[]>>('/api/observe/trace/failed');
  return res.data;
}

/**
 * Get latency breakdown for a trace
 */
export async function getTraceLatency(
  traceId: string
): Promise<ApiResponse<Record<string, number>>> {
  const res = await request.get<ApiResponse<Record<string, number>>>(
    `/api/observe/trace/${traceId}/latency`
  );
  return res.data;
}

/**
 * Delete traces older than TTL hours
 */
export async function cleanupTraces(ttlHours = 24): Promise<ApiResponse<number>> {
  const res = await request.post<ApiResponse<number>>('/api/observe/trace/cleanup', null, {
    params: { ttlHours },
  });
  return res.data;
}

/**
 * Trigger immediate metrics report push
 */
export async function pushMetricsReport(): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>('/api/observe/metrics/report');
  return res.data;
}

/**
 * Reset all in-memory metrics to zero
 */
export async function resetMetrics(): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>('/api/observe/metrics/reset');
  return res.data;
}

/**
 * Get aggregate trace statistics
 */
export async function getTraceStatistics(): Promise<ApiResponse<TraceStatistics>> {
  const res = await request.get<ApiResponse<TraceStatistics>>('/api/observe/trace/statistics');
  return res.data;
}
