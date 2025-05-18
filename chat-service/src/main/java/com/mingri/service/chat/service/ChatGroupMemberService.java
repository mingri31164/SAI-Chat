package com.mingri.service.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.MemberListDto;
import com.mingri.service.chat.repo.entity.ChatGroupMember;
import com.mingri.service.chat.repo.req.MemberListVo;

import java.util.List;
import java.util.Map;

/**
 * 聊天群成员表 服务类
 */
public interface ChatGroupMemberService extends IService<ChatGroupMember> {

    List<ChatGroupMember> getGroupMember(String groupId);

    Map<String, MemberListDto> memberList(String userId, MemberListVo memberListVo);

    List<MemberListDto> memberListPage(String userId, MemberListVo memberListVo);

    boolean isMemberExists(String groupId, String userId);
}
