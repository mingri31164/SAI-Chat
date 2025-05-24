package com.mingri.service.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.entity.MessageRetraction;
import com.mingri.service.chat.repo.mapper.MessageRetractionMapper;
import com.mingri.service.chat.service.MessageRetractionService;
import org.springframework.stereotype.Service;

/**
 * 消息撤回内容表 服务实现类
 */
@Service
public class MessageRetractionServiceImpl extends ServiceImpl<MessageRetractionMapper, MessageRetraction> implements MessageRetractionService {

}
