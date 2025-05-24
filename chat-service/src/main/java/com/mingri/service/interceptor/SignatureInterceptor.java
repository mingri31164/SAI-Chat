package com.mingri.service.interceptor;

import com.mingri.core.toolkit.SignatureUtils;
import com.mingri.model.vo.admin.entity.Conversation;
import com.mingri.service.admin.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class SignatureInterceptor implements HandlerInterceptor {

    ConversationService conversationService;

    public SignatureInterceptor(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessKey = request.getHeader("X-Access-Key");
        String timestamp = request.getHeader("X-Timestamp");
        String signature = request.getHeader("X-Signature");
        // 验证必要参数
        if (accessKey == null || timestamp == null || signature == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 验证时间戳，防止重放攻击
        long now = System.currentTimeMillis();
        long requestTime = Long.parseLong(timestamp);
        if (Math.abs(now - requestTime) > 300000) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 根据accessKey获取secretKey
        Conversation conversation = conversationService.getConversationByAccessKey(accessKey);
        if (null == conversation) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 验证签名
        String method = request.getMethod();
        String path = request.getRequestURI();
        String calculatedSignature = SignatureUtils.calculateSignature(method, path, accessKey, timestamp, conversation.getSecretKey());
        if (!calculatedSignature.equals(signature)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("accessKey", accessKey);
        map.put("timestamp", timestamp);
        map.put("userId", conversation.getUserId());
        request.setAttribute("userinfo", map);
        return true;
    }
}
