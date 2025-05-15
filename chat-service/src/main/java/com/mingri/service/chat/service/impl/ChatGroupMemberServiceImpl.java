package com.mingri.service.chat.service.impl;

import com.mingri.service.chat.repo.entity.ChatGroupMemberDO;
import com.mingri.service.chat.repo.mapper.ChatGroupMemberMapper;
import com.mingri.service.chat.service.IChatGroupMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天群成员表 服务实现类
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
@Service
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberMapper, ChatGroupMemberDO> implements IChatGroupMemberService {

    @Override
    public boolean isMemberExists(String groupId, String inviteUserid) {
        return false;
    }
}
