package com.mingri.service.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.ChatListDto;
import com.mingri.service.chat.repo.entity.ChatList;
import com.mingri.service.chat.repo.req.chatlist.CreateChatListVo;
import com.mingri.service.chat.repo.req.chatlist.DeleteChatListVo;
import com.mingri.service.chat.repo.req.chatlist.DetailChatListVo;
import com.mingri.service.chat.repo.req.chatlist.TopChatListVo;

/**
 * 聊天列表 服务类
 */
public interface ChatListService extends IService<ChatList> {

    ChatListDto getChatList(String userId);

    ChatList createChatList(String userId, String role, CreateChatListVo createChatListVo);

    boolean messageRead(String userId, String targetId);

    ChatList detailChartList(String userId, DetailChatListVo detailChatListVo);

    boolean deleteChatList(String userId, DeleteChatListVo deleteChatListVo);

    boolean topChatList(String userId, TopChatListVo topChatListVo);

    int unread(String userId);

    boolean messageReadAll(String userId);

    void removeByUserId(String userId, String friendId);

    ChatList getChatListByUserIdAndFromId(String userId, String toId);

}
