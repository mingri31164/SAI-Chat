package com.mingri.service.impl;

import com.mingri.entity.Message;
import com.mingri.mapper.MessageMapper;
import com.mingri.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-24
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

}
