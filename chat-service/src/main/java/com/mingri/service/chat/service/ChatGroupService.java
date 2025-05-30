package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.vo.chatgroup.dto.ChatGroupDetailsDto;
import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroup;
import com.mingri.service.chat.repo.vo.chatgroup.req.*;

import java.util.List;

/**
 * 聊天群表 服务类
 */
public interface ChatGroupService extends IService<ChatGroup> {

    boolean createChatGroup(String userId, CreateChatGroupReq createChatGroupVo);

    List<ChatGroup> chatGroupList(String userId);

    ChatGroupDetailsDto detailsChatGroup(String userId, DetailsChatGroupReq detailsChatGroupVo);

    boolean isOwner(String groupId, String userId);

    boolean updateGroupPortrait(String groupId, String url);

    boolean updateChatGroupName(String userId, UpdateChatGroupNameReq updateChatGroupNameVo);

    boolean updateChatGroup(String userId, UpdateChatGroupReq updateChatGroupVo);

    boolean inviteMember(String userId, InviteMemberReq inviteMemberVo);

    boolean quitChatGroup(String userId, QuitChatGroupReq quitChatGroupVo);

    boolean kickChatGroup(String userId, KickChatGroupReq kickChatGroupVo);

    boolean dissolveChatGroup(String userId, DissolveChatGroupReq dissolveChatGroupVo);

    boolean transferChatGroup(String userId, TransferChatGroupReq transferChatGroupVo);
}
