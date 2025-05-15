package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mingri.model.constant.type.ChatListType;
import com.mingri.model.context.BaseContext;
import com.mingri.model.vo.sys.dto.SysUserInfoDTO;
import com.mingri.service.chat.repo.entity.ChatGroupDO;
import com.mingri.service.chat.repo.entity.ChatListDO;
import com.mingri.service.chat.repo.entity.MessageDO;
import com.mingri.service.chat.repo.mapper.ChatListMapper;
import com.mingri.service.user.service.ISysUserService;
import com.mingri.service.chat.service.IChatListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;



@Slf4j
@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatListDO> implements IChatListService {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    //@DS("slave")
    public List<ChatListDO> privateList() {
        String currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatListDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatListDO::getUserId, currentId)
                .eq(ChatListDO::getType, ChatListType.User);
        return list(queryWrapper);
    }

    @Override
    //@DS("slave")
    public ChatListDO getGroup() {
        String userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatListDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatListDO::getUserId, userId)
                .eq(ChatListDO::getType, ChatListType.Group);
        ChatListDO chat = getOne(queryWrapper);
        if (chat == null) {
            chat = new ChatListDO();
            chat.setId(IdUtil.simpleUUID());
            chat.setType(ChatListType.Group);
            chat.setUserId(userId);
            chat.setTargetId("1");
            ChatGroupDO group = Db.lambdaQuery(ChatGroupDO.class).eq(ChatGroupDO::getId, "1").one();
            SysUserInfoDTO userDto = new SysUserInfoDTO();
            userDto.setId(group.getId());
            userDto.setName(group.getName());
            userDto.setAvatar(group.getAvatar());
            chat.setTargetInfo(userDto);
            save(chat);
        }
        return chat;
    }

    @Override
    public ChatListDO create(String targetId) {
        String userId = BaseContext.getCurrentId();
        if (userId.equals(targetId))
            return null;
        ChatListDO targetChatList = getTargetChatList(targetId);
        if (targetChatList != null) {
            return targetChatList;
        }
        SysUserInfoDTO user = sysUserService.getUserById(targetId);
        ChatListDO chatList = new ChatListDO();
        chatList.setId(IdUtil.simpleUUID());
        chatList.setUserId(userId);
        chatList.setTargetId(targetId);
        chatList.setType(ChatListType.User);
        chatList.setTargetInfo(user);
        chatList.setLastMessage(new MessageDO());
        save(chatList);
        return chatList;
    }

    /**
     * @Description: 是否已读
     * @Author: mingri31164
     * @Date: 2025/1/26 1:55
     **/
    @Override
    public boolean read(String targetId) {
        String userId = BaseContext.getCurrentId();
        if (targetId == null) return false;
        LambdaUpdateWrapper<ChatListDO> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(ChatListDO::getUnreadCount, 0)
                .eq(ChatListDO::getUserId, userId)
                .eq(ChatListDO::getTargetId, targetId);
        return update(new ChatListDO(), updateWrapper);
    }

    @Override
    public boolean delete(String chatListId) {
        return removeById(chatListId);
    }

    @Override
    public boolean updateChatListPrivate(String targetId, MessageDO message) {
        String userId = BaseContext.getCurrentId();
        //更新对方聊天列表
        updateChatList(targetId, userId, message);
        //更新自己的聊天列表
        return updateChatList(userId, targetId, message);
    }

    @Override
    public boolean updateChatListGroup(MessageDO message) {
        LambdaUpdateWrapper<ChatListDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatListDO::getLastMessage, JSONUtil.toJsonStr(message))
                .eq(ChatListDO::getType, ChatListType.Group);
        return update(updateWrapper);
    }

    public boolean updateChatList(String userId, String targetId, MessageDO message) {
        //判断聊天列表是否存在
        LambdaQueryWrapper<ChatListDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatListDO::getUserId, targetId)
                .eq(ChatListDO::getTargetId, userId);
        ChatListDO chatList = getOne(queryWrapper);
        if (null == chatList) {
            chatList = new ChatListDO();
            chatList.setId(IdUtil.randomUUID());
            chatList.setUserId(targetId);
            chatList.setType(ChatListType.User);
            chatList.setTargetId(userId);
            chatList.setUnreadCount(1);
            chatList.setTargetInfo(sysUserService.getUserById(userId));
            chatList.setLastMessage(message);
            return save(chatList);
        } else {
            chatList.setUnreadCount(chatList.getUnreadCount() + 1);
            chatList.setLastMessage(message);
            return updateById(chatList);
        }
    }

    //@DS("slave")
    private ChatListDO getTargetChatList(String targetId) {
        String userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatListDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatListDO::getTargetId, targetId)
                .eq(ChatListDO::getUserId, userId)
                .eq(ChatListDO::getType, ChatListType.User);
        return getOne(queryWrapper);
    }

}
