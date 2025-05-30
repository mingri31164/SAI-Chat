package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.constant.FriendApplyStatus;
import com.mingri.model.constant.NotifyType;
import com.mingri.model.constant.UserRole;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.repo.vo.friend.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.vo.friend.dto.FriendListDto;
import com.mingri.service.talk.repo.vo.dto.TalkContentDto;
import com.mingri.service.chat.repo.vo.friend.entity.Friend;
import com.mingri.service.chat.repo.vo.group.entity.Group;
import com.mingri.service.chat.repo.vo.message.entity.Message;
import com.mingri.service.chat.repo.vo.message.dto.MsgContent;
import com.mingri.service.chat.repo.mapper.FriendMapper;
import com.mingri.service.chat.repo.vo.friend.req.*;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.chat.service.FriendService;
import com.mingri.service.chat.service.GroupService;
import com.mingri.service.talk.service.TalkService;
import com.mingri.service.notify.repo.vo.entity.Notify;
import com.mingri.service.notify.service.NotifyService;
import com.mingri.service.rocketmq.MQProducerService;
import com.mingri.service.user.repo.vo.entity.User;
import com.mingri.service.user.service.UserService;
import com.mingri.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 好友表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Resource
    FriendMapper friendMapper;
    @Resource
    UserService userService;
    @Resource
    GroupService groupService;
    @Resource
    ChatListService chatListService;
    @Resource
    MQProducerService mqProducerService;
    @Resource
    WebSocketService webSocketService;
    @Resource
    NotifyService  notifyService;
    @Resource
    TalkService  talkService;

    @Override
    public boolean updateGroupId(String userId, String oldGroupId, String newGroupId) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getGroupId, newGroupId)
                .eq(Friend::getGroupId, oldGroupId)
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean setRemark(String userId, SetRemarkReq setRemarkVo) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getRemark, setRemarkVo.getRemark())
                .eq(Friend::getFriendId, setRemarkVo.getFriendId())
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean setGroup(String userId, SetGroupReq setGroupVo) {
        boolean isExist = groupService.IsExistGroupByUserId(userId, setGroupVo.getGroupId());
        if (!isExist) {
            throw new BaseException("分组不存在");
        }
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getGroupId, setGroupVo.getGroupId())
                .eq(Friend::getFriendId, setGroupVo.getFriendId())
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean deleteFriend(String userId, DeleteFriendReq deleteFriendVo) {
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.or((q) -> q.eq(Friend::getUserId, userId).eq(Friend::getFriendId,
                deleteFriendVo.getFriendId())).or((q) -> q.eq(Friend::getFriendId, userId).eq(Friend::getUserId,
                deleteFriendVo.getFriendId()));
        chatListService.removeByUserId(userId, deleteFriendVo.getFriendId());
        Message message = new Message();
        message.setFromId(userId);
        message.setToId(deleteFriendVo.getFriendId());
        MsgContent  msgContent = new MsgContent();
        msgContent.setContent("friend_delete");
        message.setMsgContent(msgContent);
        mqProducerService.sendMsgToUser(message);
        return remove(queryWrapper);
    }

    @Override
    public boolean careForFriend(String userId, CareForFriendReq careForFriendVo) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getIsConcern, true)
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, careForFriendVo.getFriendId());
        return update(updateWrapper);
    }

    @Override
    public boolean unCareForFriend(String userId, UnCareForFriendReq unCareForFriendReq) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getIsConcern, false)
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, unCareForFriendReq.getFriendId());
        return update(updateWrapper);
    }

    @Override
    public List<Friend> getFriendListFlat(String userId, String friendInfo) {
        return friendMapper.getFriendListFlat(userId, friendInfo);
    }

    @Override
    public List<Friend> getFriendListFlatUnread(String userId, String friendInfo) {
        return friendMapper.getFriendListFlatUnread(userId, friendInfo);
    }

    @Override
    public List<FriendDetailsDto> searchFriends(String userId, SearchReq searchReq) {
        return friendMapper.searchFriends(userId, "%" + searchReq.getSearchInfo() + "%");
    }

    /**
     * 判断是否是好友（忽略管理员，三方用户)
     */
    @Override
    public boolean isFriendIgnoreSpecial(String userId, String friendId) {
        User friend = userService.getById(friendId);
        User user = userService.getById(userId);
        if (null != user && (UserRole.Admin.equals(user.getRole()) || UserRole.Admin.equals(friend.getRole()))) {
            return true;
        }
        assert user != null;
        if (UserRole.Third.equals(user.getRole())) {
            return true;
        }
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        return count(queryWrapper) > 0;
    }

    @Override
    public List<FriendListDto> getFriendList(String userId) {
        List<FriendListDto> friendListDtos = new ArrayList<>();
        //特别关心
        List<Friend> concernFriends = friendMapper.getConcernFriendByUser(userId);
        if (null != concernFriends && !concernFriends.isEmpty()) {
            FriendListDto concernFriendListDto = new FriendListDto();
            concernFriendListDto.setName("特别关心");
            concernFriendListDto.setFriends(concernFriends);
            concernFriendListDto.setCustom(false);
            friendListDtos.add(concernFriendListDto);
        }
        //将没有分组的好像添加到未分组中
        List<Friend> ungroupFriends = friendMapper.getFriendByUserIdAndGroupId(userId, "0");
        if (null != ungroupFriends && !ungroupFriends.isEmpty()) {
            FriendListDto ungroupFriendListDto = new FriendListDto();
            ungroupFriendListDto.setName("未分组");
            ungroupFriendListDto.setFriends(ungroupFriends);
            ungroupFriendListDto.setCustom(false);
            friendListDtos.add(ungroupFriendListDto);
        }
        //查询用户当前分组
        List<Group> groups = groupService.getGroupByUserId(userId);
        //遍历分组，查询分组下的好友
        groups.forEach((group) -> {
            List<Friend> friends = friendMapper.getFriendByUserIdAndGroupId(userId, group.getId());
            FriendListDto friendListDto = new FriendListDto();
            friendListDto.setGroupId(group.getId());
            friendListDto.setName(group.getName());
            friendListDto.setFriends(friends);
            friendListDto.setCustom(true);
            friendListDtos.add(friendListDto);
        });
        return friendListDtos;
    }

    @Override
    public boolean isFriend(String userId, String friendId) {
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        return count(queryWrapper) > 0;
    }

    @Override
    public FriendDetailsDto getFriendDetails(String userId, String friendId) {
        boolean isFriend = isFriendIgnoreSpecial(userId, friendId);
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        FriendDetailsDto friendDetailsDto = friendMapper.getFriendDetails(userId, friendId);
        TalkContentDto talkContentDto = talkService.getFriendLatestTalkContent(userId, friendId);
        friendDetailsDto.setTalkContent(talkContentDto);
        return friendDetailsDto;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean agreeFriendApply(String userId, AgreeFriendApplyReq agreeFriendApplyVo) {
        //判断申请是否是用户发起
        Notify notify = notifyService.getById(agreeFriendApplyVo.getNotifyId());
        if (null == notify
                || !notify.getToId().equals(userId)
                || !notify.getType().equals(NotifyType.Friend_Apply)
                || !notify.getStatus().equals(FriendApplyStatus.Wait)
        ) {
            throw new BaseException("没有添加好友申请");
        }
        //双方添加好友
        boolean result = addFriendApply(userId, notify.getFromId());
        //更新通知
        notify.setStatus(FriendApplyStatus.Agree);
        notify.setUnreadId(notify.getFromId());
        notifyService.updateById(notify);
        //发送通知
        webSocketService.sendNotifyToUser(notify, notify.getFromId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean agreeFriendApply(String userId, String fromId) {

        LambdaQueryWrapper<Notify> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Notify::getFromId, fromId)
                .eq(Notify::getStatus, FriendApplyStatus.Wait)
                .eq(Notify::getToId, userId);

        List<Notify> notifyList = notifyService.list(queryWrapper);

        if (notifyList.isEmpty())
            throw new BaseException("没有添加好友申请");

        notifyList.forEach(notify -> {
            notify.setStatus(FriendApplyStatus.Agree);
            notify.setUnreadId(notify.getFromId());
            notifyService.updateById(notify);
            webSocketService.sendNotifyToUser(notify, notify.getFromId());
        });

        //双方添加好友

        return addFriendApply(userId, notifyList.get(0).getFromId());
    }

    @Override
    public boolean rejectFriendApply(String userId, String fromId) {

        LambdaQueryWrapper<Notify> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Notify::getFromId, fromId)
                .eq(Notify::getStatus, FriendApplyStatus.Wait)
                .eq(Notify::getToId, userId);

        List<Notify> notifyList = notifyService.list(queryWrapper);

        if (notifyList.isEmpty())
            throw new BaseException("没有添加好友申请");

        notifyList.forEach(notify -> {
            notify.setStatus(FriendApplyStatus.Reject);
            notify.setUnreadId(notify.getFromId());
            notifyService.updateById(notify);
            webSocketService.sendNotifyToUser(notify, notify.getFromId());
        });

        //判断申请是否是用户发起
//        Notify notify = notifyService.getById(notifyId);
//        if (null == notify
//                || !notify.getToId().equals(userId)
//                || !notify.getType().equals(NotifyType.Friend_Apply)
//                || !notify.getStatus().equals(FriendApplyStatus.Wait)
//        ) {
//            throw new BaseException("没有添加好友申请");
//        }
        //更新通知
//        notify.setStatus(FriendApplyStatus.Reject);
//        notify.setUnreadId(notify.getFromId());
//        boolean result = notifyService.updateById(notify);
        //发送通知
//        webSocketService.sendNotifyToUser(notify, notify.getFromId());

//        return result;

        return true;
    }




    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean addFriendApply(String userId, String targetId) {
        //双方添加好友
        addFriend(userId, targetId);
        return addFriend(targetId, userId);
    }




    /**
     * 添加好友
     *
     */
    public boolean addFriend(String userId, String targetId) {
        User user = userService.getById(userId);
        if (null == user) {
            throw new BaseException("用户不存在");
        }
        user = userService.getById(targetId);
        if (null == user) {
            throw new BaseException("用户不存在");
        }
        //判断目标是否是自己好友
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, targetId);
        if (count(queryWrapper) <= 0) {
            Friend friend = new Friend();
            friend.setId(IdUtil.randomUUID());
            friend.setUserId(userId);
            friend.setFriendId(targetId);
            return save(friend);
        }
        return true;
    }
}
