package com.mingri.service;

import com.mingri.entity.chat.ChatGroupMember;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 聊天群成员表 服务类
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
public interface IChatGroupMemberService extends IService<ChatGroupMember> {

    boolean isMemberExists(String groupId, String inviteUserid);
}
