package com.mingri.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mingri.constant.ChatListType;
import com.mingri.context.BaseContext;
import com.mingri.entity.ChatGroup;
import com.mingri.entity.ChatList;
import com.mingri.entity.Message;
import com.mingri.entity.SysUser;
import com.mingri.mapper.ChatGroupMapper;
import com.mingri.mapper.ChatListMapper;
import com.mingri.mapper.SysUserMapper;
import com.mingri.service.IChatListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.ISysUserService;
import com.mingri.vo.SysUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-24
 */
@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements IChatListService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<ChatList> privateList() {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, currentId)
                .eq(ChatList::getType, ChatListType.User);
        return list(queryWrapper);
    }

    @Override
    public ChatList getGroup() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, userId)
                .eq(ChatList::getType, ChatListType.Group);
        ChatList chat = getOne(queryWrapper);
        if (chat == null) {
            chat = new ChatList();
            chat.setId(IdUtil.simpleUUID());
            chat.setType(ChatListType.Group);
            chat.setUserId(String.valueOf(userId));
            chat.setTargetId("1");
            ChatGroup group = Db.lambdaQuery(ChatGroup.class).eq(ChatGroup::getId, "1").one();
            SysUserInfoVO userDto = new SysUserInfoVO();
            userDto.setId(1L);
            userDto.setUserName("1L");
            userDto.setAvatar(null);
            chat.setTargetInfo(userDto);
            save(chat);
        }
        return chat;
    }

    @Override
    public ChatList create(String targetId) {
        String userId = String.valueOf(BaseContext.getCurrentId());
        if (userId.equals(targetId))
            return null;
        ChatList targetChatList = getTargetChatList(targetId);
        if (targetChatList != null) {
            return targetChatList;
        }
        SysUserInfoVO user = sysUserMapper.getUserById(targetId);
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

    @Override
    public boolean read(String targetId) {
        String userId = String.valueOf(BaseContext.getCurrentId());
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


    private ChatList getTargetChatList(String targetId) {
        String userId = String.valueOf(BaseContext.getCurrentId());
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getTargetId, targetId)
                .eq(ChatList::getUserId, userId)
                .eq(ChatList::getType, ChatListType.User);
        return getOne(queryWrapper);
    }
}
