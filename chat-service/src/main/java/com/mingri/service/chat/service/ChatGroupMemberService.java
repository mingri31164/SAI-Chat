package com.mingri.service.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.vo.chatgroup.dto.MemberListDto;
import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroupMember;
import com.mingri.service.chat.repo.vo.chatlist.req.MemberListReq;

import java.util.List;
import java.util.Map;

/**
 * 聊天群成员表 服务类
 */
public interface ChatGroupMemberService extends IService<ChatGroupMember> {

    List<ChatGroupMember> getGroupMember(String groupId);

    Map<String, MemberListDto> memberList(String userId, MemberListReq memberListVo);

    List<MemberListDto> memberListPage(String userId, MemberListReq memberListVo);

    boolean isMemberExists(String groupId, String userId);
}
