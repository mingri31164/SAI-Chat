package com.mingri.service.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.entity.ChatList;
import com.mingri.service.chat.repo.mapper.ChatListMapper;
import com.mingri.service.chat.service.ChatListService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 聊天列表 服务实现类
 */
@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements ChatListService {

    @Resource
    private ChatListMapper chatListMapper;

    @Override
    public int unread(String userId) {
        Integer num = chatListMapper.unreadByUserId(userId);
        return num == null ? 0 : num;
    }

}
