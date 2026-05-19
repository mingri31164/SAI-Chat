import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import type { ABTest, TestResult, MetricRecord } from '@/types/abtest';

/**
 * Create a new A/B test
 */
export async function createABTest(test: Omit<ABTest, 'testId' | 'createdTime'>): Promise<ApiResponse<ABTest>> {
  const res = await request.post<ApiResponse<ABTest>>('/api/abtest/test', test);
  return res.data;
}

/**
 * Start a test
 */
export async function startTest(testId: string): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(`/api/abtest/test/${testId}/start`);
  return res.data;
}

/**
 * Pause a test
 */
export async function pauseTest(testId: string): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(`/api/abtest/test/${testId}/pause`);
  return res.data;
}

/**
 * End a test
 */
export async function endTest(testId: string, markWinner = true): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(`/api/abtest/test/${testId}/end`, null, {
    params: { markWinner },
  });
  return res.data;
}

/**
 * Get test details
 */
export async function getTest(testId: string): Promise<ApiResponse<ABTest>> {
  const res = await request.get<ApiResponse<ABTest>>(`/api/abtest/test/${testId}`);
  return res.data;
}

/**
 * Get test result with statistical analysis
 */
export async function getTestResult(testId: string): Promise<ApiResponse<TestResult>> {
  const res = await request.get<ApiResponse<TestResult>>(`/api/abtest/test/${testId}/result`);
  return res.data;
}

/**
 * List all tests
 */
export async function listTests(): Promise<ApiResponse<ABTest[]>> {
  const res = await request.get<ApiResponse<ABTest[]>>('/api/abtest/tests');
  return res.data;
}

/**
 * List running tests only
 */
export async function listRunningTests(): Promise<ApiResponse<ABTest[]>> {
  const res = await request.get<ApiResponse<ABTest[]>>('/api/abtest/tests/running');
  return res.data;
}

/**
 * Delete a test
 */
export async function deleteTest(testId: string): Promise<ApiResponse<void>> {
  const res = await request.delete<ApiResponse<void>>(`/api/abtest/test/${testId}`);
  return res.data;
}

/**
 * Assign user to variant (deterministic)
 */
export async function assignVariant(
  testId: string,
  userId: string
): Promise<ApiResponse<string>> {
  const res = await request.get<ApiResponse<string>>(
    `/api/abtest/test/${testId}/assign`,
    { params: { userId } }
  );
  return res.data;
}

/**
 * Record a metric value
 */
export async function recordMetric(record: MetricRecord): Promise<ApiResponse<void>> {
  const res = await request.post<ApiResponse<void>>(
    `/api/abtest/test/${record.testId}/metric`,
    record
  );
  return res.data;
}
