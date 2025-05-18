package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.ChatGroupDetailsDto;
import com.mingri.service.chat.repo.entity.ChatGroup;
import com.mingri.service.chat.repo.req.chatgroup.*;

import java.util.List;

/**
 * 聊天群表 服务类
 */
public interface ChatGroupService extends IService<ChatGroup> {

    boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo);

    List<ChatGroup> chatGroupList(String userId);

    ChatGroupDetailsDto detailsChatGroup(String userId, DetailsChatGroupVo detailsChatGroupVo);

    boolean isOwner(String groupId, String userId);

    boolean updateGroupPortrait(String groupId, String url);

    boolean updateChatGroupName(String userId, UpdateChatGroupNameVo updateChatGroupNameVo);

    boolean updateChatGroup(String userId, UpdateChatGroupVo updateChatGroupVo);

    boolean inviteMember(String userId, InviteMemberVo inviteMemberVo);

    boolean quitChatGroup(String userId, QuitChatGroupVo quitChatGroupVo);

    boolean kickChatGroup(String userId, KickChatGroupVo kickChatGroupVo);

    boolean dissolveChatGroup(String userId, DissolveChatGroupVo dissolveChatGroupVo);

    boolean transferChatGroup(String userId, TransferChatGroupVo transferChatGroupVo);
}
