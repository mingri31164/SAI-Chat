package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.core.config.MinioConfig;
import com.mingri.model.constant.MessageContentType;
import com.mingri.model.constant.MsgSource;
import com.mingri.model.constant.MsgType;
import com.mingri.model.constant.UserRole;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.repo.dto.ChatGroupDetailsDto;
import com.mingri.service.chat.repo.dto.SystemMsgDto;
import com.mingri.service.chat.repo.entity.ChatGroup;
import com.mingri.service.chat.repo.entity.ChatGroupMember;
import com.mingri.service.chat.repo.entity.ChatGroupNotice;
import com.mingri.service.chat.repo.entity.ChatList;
import com.mingri.service.chat.repo.entity.ext.MsgContent;
import com.mingri.service.chat.repo.mapper.ChatGroupMapper;
import com.mingri.service.chat.repo.mapper.ChatGroupNoticeMapper;
import com.mingri.service.chat.repo.req.SendMsgVo;
import com.mingri.service.chat.repo.req.chatgroup.*;
import com.mingri.service.chat.service.ChatGroupMemberService;
import com.mingri.service.chat.service.ChatGroupService;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.chat.service.MessageService;
import com.mingri.service.user.repo.entity.User;
import com.mingri.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 聊天群表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements ChatGroupService {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Resource
    ChatGroupMapper chatGroupMapper;

    @Resource
    MinioConfig minioConfig;

    @Resource
    ChatListService chatListService;

    @Resource
    MessageService messageService;

    @Resource
    UserService userService;

    final private ChatGroupNoticeMapper chatGroupNoticeMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo) {
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setId(IdUtil.randomUUID());
        Random random = new Random();
        int randomNumber = random.nextInt(1000000000);
        String randomString = String.format("%10d", randomNumber);
        LambdaQueryWrapper<ChatGroup> wrapper = new LambdaQueryWrapper<ChatGroup>()
                .eq(ChatGroup::getChatGroupNumber, randomString);
        while (count(wrapper) > 0){
            randomString = String.format("%10d", random.nextInt(1000000000));
            wrapper.eq(ChatGroup::getChatGroupNumber, randomString);
        }
        chatGroup.setChatGroupNumber(randomString);
        chatGroup.setName(createChatGroupVo.getName());
        chatGroup.setMemberNum(Optional.ofNullable(createChatGroupVo.getUsers()).map(ArrayList::size).orElse(0) + 1);
        chatGroup.setUserId(userId);
        chatGroup.setOwnerUserId(userId);
        chatGroup.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-group-portrait.png");
        if (createChatGroupVo.getNotice() != null){
            ChatGroupNotice chatGroupNotice = new ChatGroupNotice();
            chatGroupNotice.setId(IdUtil.randomUUID());
            chatGroupNotice.setChatGroupId(chatGroup.getId());
            chatGroupNotice.setNoticeContent(createChatGroupVo.getNotice());
            chatGroupNotice.setUserId(userId);
            chatGroupNoticeMapper.insert(chatGroupNotice);
            chatGroup.setNotice(chatGroupNotice);
        }

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
    public List<ChatGroup> chatGroupList(String userId) {
        List<ChatGroup> result = chatGroupMapper.getList(userId);
        return result;
    }

    @Override
    public ChatGroupDetailsDto detailsChatGroup(String userId, DetailsChatGroupVo detailsChatGroupVo) {
        ChatGroupDetailsDto result = chatGroupMapper.detailsChatGroup(userId, detailsChatGroupVo.getChatGroupId());
        return result;
    }

    @Override
    public boolean isOwner(String groupId, String userId) {
        ChatGroup group = getById(groupId);
        if (group.getOwnerUserId().equals(userId))
            return true;
        return false;
    }

    @Override
    public boolean updateGroupPortrait(String groupId, String url) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatGroup::getPortrait, url)
                .eq(ChatGroup::getId, groupId);
        return update(updateWrapper);
    }

    @Override
    public boolean updateChatGroupName(String userId, UpdateChatGroupNameVo updateChatGroupNameVo) {
        if (!isOwner(updateChatGroupNameVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatGroup::getName, updateChatGroupNameVo.getName())
                .eq(ChatGroup::getId, updateChatGroupNameVo.getGroupId());
        return update(updateWrapper);
    }

    @Override
    public boolean updateChatGroup(String userId, UpdateChatGroupVo updateChatGroupVo) {
        UpdateWrapper<ChatGroupMember> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(updateChatGroupVo.getUpdateKey(), updateChatGroupVo.getUpdateValue())
                .eq("chat_group_id", updateChatGroupVo.getGroupId())
                .eq("user_id", userId);
        return chatGroupMemberService.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean inviteMember(String userId, InviteMemberVo inviteMemberVo) {
        List<ChatGroupMember> members = new ArrayList<>();
        for (String inviteUserid : inviteMemberVo.getUserIds()) {
            if (chatGroupMemberService.isMemberExists(inviteMemberVo.getGroupId(), inviteUserid)) {
                continue;
            }
            ChatGroupMember member = new ChatGroupMember();
            member.setId(IdUtil.randomUUID());
            member.setUserId(inviteUserid);
            member.setChatGroupId(inviteMemberVo.getGroupId());
            members.add(member);

            //发送群消息系统消息
            SendMsgVo sendMsgVo = new SendMsgVo();
            sendMsgVo.setSource(MsgSource.Group);
            sendMsgVo.setToUserId(inviteMemberVo.getGroupId());
            MsgContent msgContent = new MsgContent();
            msgContent.setType(MessageContentType.System);
            User user = userService.getById(userId);
            User inviteUser = userService.getById(inviteUserid);
            //设置系统消息
            SystemMsgDto systemMsgDto = new SystemMsgDto();
            systemMsgDto.addEmphasizeContent(user.getName())
                    .addContent("邀请了")
                    .addEmphasizeContent(inviteUser.getName())
                    .addContent("加入了该群");
            msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
            msgContent.setFormUserId(userId);
            msgContent.setExt(userId);
            sendMsgVo.setMsgContent(msgContent);
            messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        }
        if (members.size() > 0) {
            ChatGroup chatGroup = getById(inviteMemberVo.getGroupId());
            chatGroup.setMemberNum(chatGroup.getMemberNum() + members.size());
            updateById(chatGroup);
            return chatGroupMemberService.saveBatch(members);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean quitChatGroup(String userId, QuitChatGroupVo quitChatGroupVo) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, quitChatGroupVo.getGroupId());
        chatGroupMemberService.remove(queryWrapper);

        LambdaQueryWrapper<ChatList> chatListLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatListLambdaQueryWrapper.eq(ChatList::getUserId, userId)
                .eq(ChatList::getFromId, quitChatGroupVo.getGroupId());
        chatListService.remove(chatListLambdaQueryWrapper);

        //发送群消息系统消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(quitChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        User user = userService.getById(userId);
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent(user.getName())
                .addContent("已退出该群");
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFormUserId(userId);
        msgContent.setExt(userId);
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        ChatGroup chatGroup = getById(quitChatGroupVo.getGroupId());
        chatGroup.setMemberNum(chatGroup.getMemberNum() - 1);
        return updateById(chatGroup);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean kickChatGroup(String userId, KickChatGroupVo kickChatGroupVo) {
        if (!isOwner(kickChatGroupVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, kickChatGroupVo.getGroupId())
                .eq(ChatGroupMember::getUserId, kickChatGroupVo.getUserId());
        chatGroupMemberService.remove(queryWrapper);

        //发送群消息系统消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(kickChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        User user = userService.getById(kickChatGroupVo.getUserId());
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent(user.getName())
                .addContent("已被踢出该群");
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFormUserId(userId);
        msgContent.setExt(kickChatGroupVo.getUserId());
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        ChatGroup chatGroup = getById(kickChatGroupVo.getGroupId());
        chatGroup.setMemberNum(chatGroup.getMemberNum() - 1);
        return updateById(chatGroup);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean dissolveChatGroup(String userId, DissolveChatGroupVo dissolveChatGroupVo) {
        if (!isOwner(dissolveChatGroupVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");

        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, dissolveChatGroupVo.getGroupId());
        chatGroupMemberService.remove(queryWrapper);

        //发送群消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(dissolveChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        msgContent.setFormUserId(userId);
        msgContent.setExt("all");
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        return removeById(dissolveChatGroupVo.getGroupId());
    }

    @Override
    public boolean transferChatGroup(String userId, TransferChatGroupVo transferChatGroupVo) {
        if (!isOwner(transferChatGroupVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        ChatGroup chatGroup = getById(transferChatGroupVo.getGroupId());
        chatGroup.setOwnerUserId(transferChatGroupVo.getUserId());
        return updateById(chatGroup);
    }
}
