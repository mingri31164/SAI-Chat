package com.mingri.ai.server;

import com.mingri.ai.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG主服务实现
 */
@Service
public class RagServiceImpl implements IRAGService {
    @Autowired
    private RagRetriever ragRetriever;
    @Autowired
    private IAiService aiService;

//    @Override
//    public String chatWithRag(String userQuery) {
//        List<String> docs = ragRetriever.retrieveRelevantDocs(userQuery, 3);
//        String context = String.join("\n", docs);
//        return aiService.chatWithContext(userQuery, context);
//    }

    @Override
    public Response<List<String>> queryRagTagList() {
        return null;
    }

    @Override
    public Response<String> uploadFile(String ragTag, List<MultipartFile> files) {
        return null;
    }

    @Override
    public Response<String> analyzeGitRepository(String repoUrl, String userName, String token) throws Exception {
        return null;
    }
}