package com.mingri.service;

import com.mingri.entity.ChatList;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.entity.Message;
import java.util.List;


public interface IChatListService extends IService<ChatList> {
    List<ChatList> privateList();
    ChatList getGroup();
    ChatList create(String targetId);

    boolean read(String targetId);

    boolean delete(String chatListId);

    boolean updateChatListPrivate(String targetId, Message message);

    boolean updateChatListGroup(Message message);
}
