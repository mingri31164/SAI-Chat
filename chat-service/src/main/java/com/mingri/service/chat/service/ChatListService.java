package com.mingri.service.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.vo.chatlist.dto.ChatListDto;
import com.mingri.service.chat.repo.vo.chatlist.entity.ChatList;
import com.mingri.service.chat.repo.vo.chatlist.req.CreateChatListReq;
import com.mingri.service.chat.repo.vo.chatlist.req.DeleteChatListReq;
import com.mingri.service.chat.repo.vo.chatlist.req.DetailChatListReq;
import com.mingri.service.chat.repo.vo.chatlist.req.TopChatListReq;
import com.mingri.service.chat.repo.vo.message.dto.MsgContent;

/**
 * 聊天列表 服务类
 */
public interface ChatListService extends IService<ChatList> {

    ChatListDto getChatList(String userId);

    ChatList createChatList(String userId, String role, CreateChatListReq createChatListVo);

    boolean messageRead(String userId, String targetId);

    ChatList detailChartList(String userId, DetailChatListReq detailChatListVo);

    boolean deleteChatList(String userId, DeleteChatListReq deleteChatListVo);

    boolean topChatList(String userId, TopChatListReq topChatListVo);

    int unread(String userId);

    boolean messageReadAll(String userId);

    void removeByUserId(String userId, String friendId);

    ChatList getChatListByUserIdAndFromId(String userId, String toId);

    void updateChatList(String toId, String userId, MsgContent msgContent, String user);

    void updateChatListGroup(String toId, MsgContent msgContent);

}
