import request from '@/utils/request';
import type { ApiResponse } from '@/types/agent';
import type { DocumentUploadResponse, TagInfo } from '@/types/document';

/**
 * Upload files to a knowledge base tag
 */
export async function uploadFiles(
  ragTag: string,
  files: File[]
): Promise<ApiResponse<DocumentUploadResponse>> {
  const formData = new FormData();
  formData.append('ragTag', ragTag);
  files.forEach((file) => formData.append('file', file));

  const res = await request.post<ApiResponse<DocumentUploadResponse>>(
    '/api/v1/rag/file/upload',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  );
  return res.data;
}

/**
 * Get all known knowledge base tags
 */
export async function getRagTagList(): Promise<ApiResponse<TagInfo[]>> {
  const res = await request.get<ApiResponse<TagInfo[]>>('/api/v1/rag/query_rag_tag_list');
  return res.data;
}

/**
 * Analyze and ingest a Git repository
 */
export async function analyzeGitRepository(
  repoUrl: string,
  userName: string,
  token: string
): Promise<ApiResponse<{ success: boolean; message: string }>> {
  const res = await request.post<ApiResponse<{ success: boolean; message: string }>>(
    '/api/v1/rag/analyze_git_repository',
    null,
    {
      params: { repoUrl, userName, token },
    }
  );
  return res.data;
}
