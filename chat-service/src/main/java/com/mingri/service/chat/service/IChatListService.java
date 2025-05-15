package com.mingri.service.chat.service;

import com.mingri.service.chat.repo.entity.ChatListDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.entity.MessageDO;
import java.util.List;


public interface IChatListService extends IService<ChatListDO> {
    List<ChatListDO> privateList();
    ChatListDO getGroup();
    ChatListDO create(String targetId);

    boolean read(String targetId);

    boolean delete(String chatListId);

    boolean updateChatListPrivate(String targetId, MessageDO message);

    boolean updateChatListGroup(MessageDO message);
}
