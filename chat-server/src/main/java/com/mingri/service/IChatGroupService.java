package com.mingri.service;

import com.mingri.entity.chat.ChatGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.vo.chatGroup.*;

import java.util.List;

/**
 * <p>
 * 聊天群表 服务类
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
public interface IChatGroupService extends IService<ChatGroup> {

    /**
     * 获取群列表
     */
    List<ChatGroup> chatGroupList(String userId);

    /**
     * 创建群聊
     **/
    boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo);

    /**
     * 更新群聊信息
     **/
    boolean updateChatGroup(String userId, UpdateChatGroupVo updateChatGroupVo);

    /**
     * 解散群聊
     **/
    boolean dissolveChatGroup(String userId, DissolveChatGroupVo dissolveChatGroupVo);

    /**
     * 是否是群主
     **/
    boolean isOwner(String groupId, String userId);

    /**
     * 邀请成员
     **/
    boolean inviteMember(String userId, InviteMemberVo inviteMemberVo);

    /**
     * 退出群聊
     **/
    boolean quitChatGroup(String userId, QuitChatGroupVo quitChatGroupVo);

    /**
     * 踢出群聊
     **/
    boolean kickChatGroup(String userId, KickChatGroupVo kickChatGroupVo);
}
