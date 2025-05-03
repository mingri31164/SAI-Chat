package com.mingri.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mingri.constant.type.ChatListType;
import com.mingri.context.BaseContext;
import com.mingri.entity.ChatGroup;
import com.mingri.entity.ChatList;
import com.mingri.entity.Message;
import com.mingri.mapper.ChatListMapper;
import com.mingri.service.IChatListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.ISysUserService;
import com.mingri.vo.SysUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;



@Slf4j
@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements IChatListService {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    //@DS("slave")
    public List<ChatList> privateList() {
        String currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, currentId)
                .eq(ChatList::getType, ChatListType.User);
        return list(queryWrapper);
    }

    @Override
    //@DS("slave")
    public ChatList getGroup() {
        String userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, userId)
                .eq(ChatList::getType, ChatListType.Group);
        ChatList chat = getOne(queryWrapper);
        if (chat == null) {
            chat = new ChatList();
            chat.setId(IdUtil.simpleUUID());
            chat.setType(ChatListType.Group);
            chat.setUserId(userId);
            chat.setTargetId("1");
            ChatGroup group = Db.lambdaQuery(ChatGroup.class).eq(ChatGroup::getId, "1").one();
            SysUserInfoVO userDto = new SysUserInfoVO();
            userDto.setId(group.getId());
            userDto.setName(group.getName());
            userDto.setAvatar(group.getAvatar());
            chat.setTargetInfo(userDto);
            save(chat);
        }
        return chat;
    }

    @Override
    public ChatList create(String targetId) {
        String userId = BaseContext.getCurrentId();
        if (userId.equals(targetId))
            return null;
        ChatList targetChatList = getTargetChatList(targetId);
        if (targetChatList != null) {
            return targetChatList;
        }
        SysUserInfoVO user = sysUserService.getUserById(targetId);
        ChatList chatList = new ChatList();
        chatList.setId(IdUtil.simpleUUID());
        chatList.setUserId(userId);
        chatList.setTargetId(targetId);
        chatList.setType(ChatListType.User);
        chatList.setTargetInfo(user);
        chatList.setLastMessage(new Message());
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
        LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(ChatList::getUnreadCount, 0)
                .eq(ChatList::getUserId, userId)
                .eq(ChatList::getTargetId, targetId);
        return update(new ChatList(), updateWrapper);
    }

    @Override
    public boolean delete(String chatListId) {
        return removeById(chatListId);
    }

    @Override
    public boolean updateChatListPrivate(String targetId, Message message) {
        String userId = BaseContext.getCurrentId();
        //更新对方聊天列表
        updateChatList(targetId, userId, message);
        //更新自己的聊天列表
        return updateChatList(userId, targetId, message);
    }

    @Override
    public boolean updateChatListGroup(Message message) {
        LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatList::getLastMessage, JSONUtil.toJsonStr(message))
                .eq(ChatList::getType, ChatListType.Group);
        return update(updateWrapper);
    }

    public boolean updateChatList(String userId, String targetId, Message message) {
        //判断聊天列表是否存在
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, targetId)
                .eq(ChatList::getTargetId, userId);
        ChatList chatList = getOne(queryWrapper);
        if (null == chatList) {
            chatList = new ChatList();
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
    private ChatList getTargetChatList(String targetId) {
        String userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getTargetId, targetId)
                .eq(ChatList::getUserId, userId)
                .eq(ChatList::getType, ChatListType.User);
        return getOne(queryWrapper);
    }

}
