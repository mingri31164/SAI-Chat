package com.mingri.service;


import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mingri.properties.ChatProperties;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.volcengine.ark.runtime.service.ArkService;

@Service
public class DoubaoAiService {

    static ArkService service;

    private final Cache<String, AtomicInteger> limitCache;

    @Autowired
    private ChatProperties chatProperties;

    DoubaoAiService() {
        this.limitCache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
    }

    @PostConstruct
    public void init() {
        String apiKey = chatProperties.getDoubao().getApiKey();
        ConnectionPool connectionPool = new ConnectionPool(10, 20, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        service = ArkService.builder().dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
                .apiKey(apiKey).build();
    }

    public String ask(String userId, String content) {
        AtomicInteger count = limitCache.getIfPresent(userId);
        if (count == null) {
            count = new AtomicInteger(0);
            limitCache.put(userId, count);
        }
        if (chatProperties.getDoubao().getCountLimit() > 0
                && count.incrementAndGet() > chatProperties.getDoubao().getCountLimit()) {
            return "您已经达到限制了，请24小时后再来吧~";
        }
        if (StrUtil.isBlank(content)) return "内容不能为空~";
        if (chatProperties.getDoubao().getLengthLimit() > 0 &&
                content.length() > chatProperties.getDoubao().getLengthLimit()) {
            return "问一些简单的问题吧~";
        }
        count.addAndGet(1);
        try {
            final List<ChatMessage> messages = new ArrayList<>();
            final ChatMessage userMessage = ChatMessage.builder().
                    role(ChatMessageRole.USER).content(content).build();
            messages.add(userMessage);
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(chatProperties.getDoubao().getModel())
                    .messages(messages)
                    .build();
            StringBuffer sb = new StringBuffer();
            service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> sb.append(choice.getMessage().getContent()).append("\n"));
            return sb.toString();
        } catch (Exception e) {
            return "豆包已离家出走了，请稍后再试~";
        }
    }
}
