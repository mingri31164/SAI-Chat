package com.mingri.service.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.entity.ChatList;

/**
 * 聊天列表 服务类
 */
public interface ChatListService extends IService<ChatList> {

    int unread(String userId);

}
