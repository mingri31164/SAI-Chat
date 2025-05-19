package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.constant.MsgSource;
import com.mingri.model.constant.UserRole;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.repo.dto.ChatListDto;
import com.mingri.service.chat.repo.entity.ChatList;
import com.mingri.service.chat.repo.mapper.ChatListMapper;
import com.mingri.service.chat.repo.req.chatlist.CreateChatListVo;
import com.mingri.service.chat.repo.req.chatlist.DeleteChatListVo;
import com.mingri.service.chat.repo.req.chatlist.DetailChatListVo;
import com.mingri.service.chat.repo.req.chatlist.TopChatListVo;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.chat.service.FriendService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天列表 服务实现类
 */
@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements ChatListService {

    @Resource
    ChatListMapper chatListMapper;

    @Resource
    @Lazy
    FriendService friendService;


    private List<ChatList> getChatListByUserIdAndIsTop(String userId, boolean isTop) {
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, userId)
                .eq(ChatList::getIsTop, isTop)
                .orderByDesc(ChatList::getUpdateTime);
        return list(queryWrapper);
    }

    public List<ChatList> mergeSortedLists(List<ChatList> list1, List<ChatList> list2) {
        List<ChatList> mergedList = new ArrayList<>();
        int i = 0, j = 0;
        while (i < list1.size() && j < list2.size()) {
            // 如果list1的当前元素的updateTime大于等于list2的当前元素，则将list1的当前元素加入mergedList
            if (list1.get(i).getUpdateTime().compareTo(list2.get(j).getUpdateTime()) >= 0) {
                mergedList.add(list1.get(i));
                i++;
            } else {
                // 否则将list2的当前元素加入mergedList
                mergedList.add(list2.get(j));
                j++;
            }
        }
        // 将list1中剩余的元素加入mergedList
        while (i < list1.size()) {
            mergedList.add(list1.get(i));
            i++;
        }
        // 将list2中剩余的元素加入mergedList
        while (j < list2.size()) {
            mergedList.add(list2.get(j));
            j++;
        }
        return mergedList;
    }

    @Override
    public ChatListDto getChatList(String userId) {
        ChatListDto chatListDto = new ChatListDto();
        //置顶
        List<ChatList> top = mergeSortedLists(chatListMapper.getChatListByUserIdAndIsTop(userId, true),
                chatListMapper.getChatListChatGroupByUserIdAndIsTop(userId, true));
        chatListDto.setTops(top);
        //其他
        List<ChatList> noTop = mergeSortedLists(chatListMapper.getChatListByUserIdAndIsTop(userId, false),
                chatListMapper.getChatListChatGroupByUserIdAndIsTop(userId, false));
        chatListDto.setOthers(noTop);
        return chatListDto;
    }


    @Override
    public ChatList createChatList(String userId, String role, CreateChatListVo createChatListVo) {
        ChatList chatList;
        if (MsgSource.User.equals(createChatListVo.getType())) {
            boolean isFriend = friendService.isFriendIgnoreSpecial(userId, createChatListVo.getToId());
            if (!isFriend && UserRole.User.equals(role)) {
                throw new BaseException("双方非好友");
            }
            chatList = chatListMapper.detailChatList(userId, createChatListVo.getToId());
        } else {
            chatList = chatListMapper.detailChatGroupList(userId, createChatListVo.getToId());
        }
        //查询是否有会话,没有则新建
        if (null != chatList)
            return chatList;
        chatList = new ChatList();
        chatList.setId(IdUtil.randomUUID());
        chatList.setUserId(userId);
        chatList.setType(createChatListVo.getType());
        chatList.setFromId(createChatListVo.getToId());
        chatList.setUnreadNum(0);
        save(chatList);
        if (MsgSource.User.equals(createChatListVo.getType())) {
            chatList = chatListMapper.detailChatList(userId, createChatListVo.getToId());
        } else {
            chatList = chatListMapper.detailChatGroupList(userId, createChatListVo.getToId());
        }
        return chatList;
    }

    @Override
    public boolean messageRead(String userId, String targetId) {
        LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatList::getUnreadNum, 0).
                eq(ChatList::getUserId, userId).
                eq(ChatList::getFromId, targetId);
        return update(updateWrapper);
    }

    @Override
    public ChatList detailChartList(String userId, DetailChatListVo detailChatListVo) {
        if (MsgSource.Group.equals(detailChatListVo.getType())) {
            return chatListMapper.detailChatGroupList(userId, detailChatListVo.getTargetId());
        } else {
            return chatListMapper.detailChatList(userId, detailChatListVo.getTargetId());
        }
    }

    @Override
    public boolean deleteChatList(String userId, DeleteChatListVo deleteChatListVo) {
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getId, deleteChatListVo.getChatListId())
                .eq(ChatList::getUserId, userId);
        return remove(queryWrapper);
    }

    @Override
    public boolean topChatList(String userId, TopChatListVo topChatListVo) {
        LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatList::getIsTop, topChatListVo.isTop())
                .eq(ChatList::getId, topChatListVo.getChatListId())
                .eq(ChatList::getUserId, userId);
        return update(new ChatList(), updateWrapper);
    }

    @Override
    public int unread(String userId) {
        Integer num = chatListMapper.unreadByUserId(userId);
        return num == null ? 0 : num;
    }

    @Override
    public boolean messageReadAll(String userId) {
        LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatList::getUnreadNum, 0).
                eq(ChatList::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public void removeByUserId(String userId, String friendId) {
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ChatList::getUserId, userId)
                .eq(ChatList::getFromId, friendId);
        remove(queryWrapper);
    }


}
