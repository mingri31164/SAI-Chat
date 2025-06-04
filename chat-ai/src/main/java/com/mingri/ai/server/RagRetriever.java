package com.mingri.ai.server;

import java.util.List;

/**
 * RAG 检索器接口
 */
public interface RagRetriever {
    /**
     * 检索相关文档
     * @param query 用户查询
     * @param topK 返回文档数
     * @return 相关文档内容列表
     */
    List<String> retrieveRelevantDocs(String query, int topK);
} 