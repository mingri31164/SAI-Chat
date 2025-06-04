package com.mingri.ai.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MCPServiceImpl implements MCPService {
    @Autowired
    private AiFacadeService aiFacadeService;

    @Override
    public String process(ChannelType channelType, Object payload) {
        switch (channelType) {
            case TEXT:
                return aiFacadeService.chat((String) payload);
            case IMAGE:
                return aiFacadeService.analyzeImage((byte[]) payload);
            case AUDIO:
                return aiFacadeService.transcribeAudio((byte[]) payload);
            // 其他类型可扩展
            default:
                return "暂不支持该通道类型";
        }
    }
} 