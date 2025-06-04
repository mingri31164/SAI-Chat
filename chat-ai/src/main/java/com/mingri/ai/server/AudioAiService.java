package com.mingri.ai.server;

import org.springframework.stereotype.Service;

@Service
public class AudioAiService {
    public String transcribeAudio(byte[] audioBytes) {
        // TODO: 调用语音转写AI服务
        return "语音转写文本";
    }
} 