package com.mingri.ai.server;

/**
 * 多通道AI消息处理服务
 */
public interface MCPService {
    /**
     * 统一处理多通道消息
     * @param channelType 消息通道类型
     * @param payload 消息内容（文本、图片、音频等）
     * @return 处理结果
     */
    String process(ChannelType channelType, Object payload);
} 