package com.mingri.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mingri.entity.ChatGroup;
import com.mingri.entity.ChatGroupMember;
import com.mingri.exception.ChatGroupOperationErrorException;
import com.mingri.mapper.ChatGroupMapper;
import com.mingri.service.IChatGroupMemberService;
import com.mingri.service.IChatGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.vo.chatGroup.CreateChatGroupVo;
import com.mingri.vo.chatGroup.DissolveChatGroupVo;
import com.mingri.vo.chatGroup.UpdateChatGroupVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.mingri.constant.MessageConstant.ERROR_ONLY_OWNER_OPERATION;

/**
 * <p>
 * 聊天群表 服务实现类
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements IChatGroupService {

    @Resource
    private ChatGroupMapper chatGroupMapper;
    @Resource
    private IChatGroupMemberService chatGroupMemberService;


    @Override
    public List<ChatGroup> chatGroupList(String userId) {
        List<ChatGroup> result = chatGroupMapper.getList(userId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo) {
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setId(IdUtil.randomUUID());
        Random random = new Random();
        int randomNumber = random.nextInt(1000000000);
        String randomString = String.format("0%10d", randomNumber);
        LambdaQueryWrapper<ChatGroup> wrapper = new LambdaQueryWrapper<ChatGroup>()
                .eq(ChatGroup::getChatGroupNumber, randomString);
        while (count(wrapper) > 0) {
            randomString = String.format("%010d", random.nextInt(1000000000));
            wrapper = new LambdaQueryWrapper<ChatGroup>()
                    .eq(ChatGroup::getChatGroupNumber, randomString);
        }

        chatGroup.setChatGroupNumber(randomString);
        chatGroup.setName(createChatGroupVo.getName());
        chatGroup.setMemberNum(Optional.ofNullable(createChatGroupVo.getUsers()).map(ArrayList::size).orElse(0) + 1);
        chatGroup.setUserId(userId);
        chatGroup.setOwnerUserId(userId);
//        TODO 设置群聊头像
//        chatGroup.setAvatar(createChatGroupVo.getAvatar());

        boolean isSava = save(chatGroup);
        //添加自己
        ChatGroupMember chatGroupMember = new ChatGroupMember();
        chatGroupMember.setId(IdUtil.randomUUID());
        chatGroupMember.setChatGroupId(chatGroup.getId());
        chatGroupMember.setUserId(userId);
        chatGroupMemberService.save(chatGroupMember);
        if (isSava && null != createChatGroupVo.getUsers()) {
            for (CreateChatGroupVo.User user : createChatGroupVo.getUsers()) {
                chatGroupMember = new ChatGroupMember();
                chatGroupMember.setId(IdUtil.randomUUID());
                chatGroupMember.setChatGroupId(chatGroup.getId());
                chatGroupMember.setUserId(user.getUserId());
                chatGroupMember.setGroupRemark(user.getName());
                chatGroupMemberService.save(chatGroupMember);
            }
        }
        return isSava;
    }

    @Override
    public boolean updateChatGroup(String userId, UpdateChatGroupVo updateChatGroupVo) {
        UpdateWrapper<ChatGroupMember> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(updateChatGroupVo.getUpdateKey(), updateChatGroupVo.getUpdateValue())
                .eq("chat_group_id", updateChatGroupVo.getGroupId())
                .eq("user_id", userId);
        return chatGroupMemberService.update(updateWrapper);
    }



}
