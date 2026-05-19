import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import type {
  PermissionContext,
  PermissionResult,
  ContentSafetyResult,
} from '@/types/document';

/**
 * Validate a single permission context
 */
export async function validatePermission(
  ctx: PermissionContext
): Promise<ApiResponse<PermissionResult>> {
  const res = await request.post<ApiResponse<PermissionResult>>(
    '/api/security/permission/validate',
    ctx
  );
  return res.data;
}

/**
 * Batch validate permission contexts
 */
export async function batchValidatePermission(
  contexts: PermissionContext[]
): Promise<ApiResponse<PermissionResult[]>> {
  const res = await request.post<ApiResponse<PermissionResult[]>>(
    '/api/security/permission/batch-validate',
    contexts
  );
  return res.data;
}

/**
 * Check if a user has a specific permission
 */
export async function checkPermission(
  userId: string,
  permission: string
): Promise<ApiResponse<boolean>> {
  const res = await request.get<ApiResponse<boolean>>(
    '/api/security/permission/check',
    { params: { userId, permission } }
  );
  return res.data;
}

/**
 * Get all permissions for a user
 */
export async function getUserPermissions(
  userId: string
): Promise<ApiResponse<string[]>> {
  const res = await request.get<ApiResponse<string[]>>(
    `/api/security/permission/user/${userId}`
  );
  return res.data;
}

/**
 * Filter user input for safety
 */
export async function filterInput(
  content: string
): Promise<ApiResponse<ContentSafetyResult>> {
  const res = await request.post<ApiResponse<ContentSafetyResult>>(
    '/api/security/content/filter-input',
    { content }
  );
  return res.data;
}

/**
 * Filter model output for safety
 */
export async function filterOutput(
  content: string
): Promise<ApiResponse<ContentSafetyResult>> {
  const res = await request.post<ApiResponse<ContentSafetyResult>>(
    '/api/security/content/filter-output',
    { content }
  );
  return res.data;
}

/**
 * Get all content safety rules
 */
export async function getSafetyRules(): Promise<
  ApiResponse<{ sensitiveWords: string[]; promptInjection: string[]; codeInjection: string[] }>
> {
  const res = await request.get('/api/security/content/rules');
  return res.data;
}

/**
 * Get violation count for a user
 */
export async function getViolationCount(userId: string): Promise<ApiResponse<number>> {
  const res = await request.get<ApiResponse<number>>('/api/security/violation/count', {
    params: { userId },
  });
  return res.data;
}
