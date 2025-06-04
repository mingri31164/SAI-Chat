package com.mingri.ai.server;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiServiceImpl implements IAiService {
    @Override
    public ChatResponse generate(String model, String message) {
        return null;
    }

    @Override
    public Flux<ChatResponse> generateStream(String model, String message) {
        return null;
    }

    @Override
    public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message) {
        return null;
    }

    @Override
    public String chatWithContext(String userQuery, String context) {
        // 这里应集成SpringAI/OpenAI等大模型API，示例为拼接prompt
        String prompt = "已知资料：" + context + "\n用户问题：" + userQuery;
        // TODO: 调用大模型API
        return "[AI回复] " + prompt;
    }
}