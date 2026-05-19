import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import type { SuccessPattern, FailureLesson, ReviewReport, AggregatedReview, ReviewStats } from '@/types/document';

/**
 * Perform single trace review
 */
export async function analyzeTrace(traceId?: string, sessionId?: string): Promise<ApiResponse<ReviewReport>> {
  const res = await request.post<ApiResponse<ReviewReport>>('/api/review/analyze', null, {
    params: { traceId, sessionId },
  });
  return res.data;
}

/**
 * Batch review multiple traces
 */
export async function batchReview(traceIds: string[]): Promise<ApiResponse<ReviewReport[]>> {
  const res = await request.post<ApiResponse<ReviewReport[]>>('/api/review/batch', traceIds);
  return res.data;
}

/**
 * Aggregate multiple review reports into summary
 */
export async function aggregateReviews(
  reports: ReviewReport[]
): Promise<ApiResponse<AggregatedReview>> {
  const res = await request.post<ApiResponse<AggregatedReview>>('/api/review/aggregate', reports);
  return res.data;
}

/**
 * Add a success pattern
 */
export async function addPattern(pattern: Omit<SuccessPattern, 'patternId' | 'accessCount' | 'createdAt'>): Promise<ApiResponse<SuccessPattern>> {
  const res = await request.post<ApiResponse<SuccessPattern>>('/api/review/pattern', pattern);
  return res.data;
}

/**
 * Get a pattern by ID
 */
export async function getPattern(patternId: string): Promise<ApiResponse<SuccessPattern>> {
  const res = await request.get<ApiResponse<SuccessPattern>>(`/api/review/pattern/${patternId}`);
  return res.data;
}

/**
 * Search patterns by keyword
 */
export async function searchPatterns(query: string): Promise<ApiResponse<SuccessPattern[]>> {
  const res = await request.get<ApiResponse<SuccessPattern[]>>('/api/review/patterns/search', {
    params: { query },
  });
  return res.data;
}

/**
 * Get all patterns
 */
export async function getAllPatterns(): Promise<ApiResponse<SuccessPattern[]>> {
  const res = await request.get<ApiResponse<SuccessPattern[]>>('/api/review/patterns');
  return res.data;
}

/**
 * Get top-N most effective patterns
 */
export async function getTopPatterns(limit = 10): Promise<ApiResponse<SuccessPattern[]>> {
  const res = await request.get<ApiResponse<SuccessPattern[]>>('/api/review/patterns/top', {
    params: { limit },
  });
  return res.data;
}

/**
 * Get patterns related to context
 */
export async function getRelatedPatterns(context: string): Promise<ApiResponse<SuccessPattern[]>> {
  const res = await request.get<ApiResponse<SuccessPattern[]>>('/api/review/patterns/related', {
    params: { context },
  });
  return res.data;
}

/**
 * Add a failure lesson
 */
export async function addLesson(lesson: Omit<FailureLesson, 'lessonId' | 'createdAt'>): Promise<ApiResponse<FailureLesson>> {
  const res = await request.post<ApiResponse<FailureLesson>>('/api/review/lesson', lesson);
  return res.data;
}

/**
 * Get a lesson by ID
 */
export async function getLesson(lessonId: string): Promise<ApiResponse<FailureLesson>> {
  const res = await request.get<ApiResponse<FailureLesson>>(`/api/review/lesson/${lessonId}`);
  return res.data;
}

/**
 * Search lessons
 */
export async function searchLessons(query: string): Promise<ApiResponse<FailureLesson[]>> {
  const res = await request.get<ApiResponse<FailureLesson[]>>('/api/review/lessons/search', {
    params: { query },
  });
  return res.data;
}

/**
 * Get all lessons
 */
export async function getAllLessons(): Promise<ApiResponse<FailureLesson[]>> {
  const res = await request.get<ApiResponse<FailureLesson[]>>('/api/review/lessons');
  return res.data;
}

/**
 * Adjust pattern score
 */
export async function adjustPatternScore(
  patternId: string,
  delta: number
): Promise<ApiResponse<void>> {
  const res = await request.put<ApiResponse<void>>(
    `/api/review/pattern/${patternId}/score`,
    null,
    { params: { delta } }
  );
  return res.data;
}

/**
 * Delete a pattern
 */
export async function deletePattern(patternId: string): Promise<ApiResponse<void>> {
  const res = await request.delete<ApiResponse<void>>(`/api/review/pattern/${patternId}`);
  return res.data;
}

/**
 * Delete a lesson
 */
export async function deleteLesson(lessonId: string): Promise<ApiResponse<void>> {
  const res = await request.delete<ApiResponse<void>>(`/api/review/lesson/${lessonId}`);
  return res.data;
}

/**
 * Export all patterns and lessons
 */
export async function exportReviewData(): Promise<ApiResponse<{ patterns: SuccessPattern[]; lessons: FailureLesson[] }>> {
  const res = await request.get<ApiResponse<{ patterns: SuccessPattern[]; lessons: FailureLesson[] }>>('/api/review/export');
  return res.data;
}

/**
 * Get repository statistics
 */
export async function getReviewStats(): Promise<ApiResponse<ReviewStats>> {
  const res = await request.get<ApiResponse<ReviewStats>>('/api/review/stats');
  return res.data;
}
