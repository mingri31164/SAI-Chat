import request from '@/utils/request';
import type { AgentChatRequest, AgentResponse, ApiResponse } from '@/types/agent';

/**
 * Synchronous chat (blocking, returns full response)
 */
export async function chatSync(data: AgentChatRequest): Promise<ApiResponse<AgentResponse>> {
  const res = await request.post<ApiResponse<AgentResponse>>('/agent/chat', data);
  return res.data;
}

/**
 * List available agent names
 */
export async function listAgents(): Promise<ApiResponse<string[]>> {
  const res = await request.get<ApiResponse<string[]>>('/agent/agents');
  return res.data;
}

/**
 * Get agent session status
 */
export async function getSessionStatus(sessionId: string): Promise<ApiResponse<string>> {
  const res = await request.get<ApiResponse<string>>(`/agent/status/${sessionId}`);
  return res.data;
}
