package com.mingri.service.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.vo.chatgroup.dto.MemberListDto;
import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroupMember;
import com.mingri.service.chat.repo.mapper.ChatGroupMemberMapper;
import com.mingri.service.chat.repo.vo.chatlist.req.MemberListReq;
import com.mingri.service.chat.service.ChatGroupMemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天群成员表 服务实现类
 */
@Service
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberMapper, ChatGroupMember> implements ChatGroupMemberService {

    @Resource
    ChatGroupMemberMapper chatGroupMemberMapper;

    @Override
    public List<ChatGroupMember> getGroupMember(String groupId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, groupId);
        return list(queryWrapper);
    }

    @Override
    public Map<String, MemberListDto> memberList(String userId, MemberListReq memberListVo) {
        List<MemberListDto> result = chatGroupMemberMapper.memberList(userId, memberListVo.getChatGroupId());
        return result.stream().collect(Collectors.toMap(MemberListDto::getUserId, user -> user));
    }

    @Override
    public List<MemberListDto> memberListPage(String userId, MemberListReq memberListVo) {
        List<MemberListDto> result = chatGroupMemberMapper.memberListPage(userId, memberListVo);
        return result;
    }

    @Override
    public boolean isMemberExists(String groupId, String userId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, groupId);
        return count(queryWrapper) > 0;
    }
}
