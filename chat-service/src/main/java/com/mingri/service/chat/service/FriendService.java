package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.dto.FriendListDto;
import com.mingri.service.chat.repo.entity.Friend;
import com.mingri.service.chat.repo.req.friend.*;

import java.util.List;

/**
 * 好友表 服务类
 */
public interface FriendService extends IService<Friend> {


    boolean updateGroupId(String userId, String oldGroupId, String newGroupId);

    List<FriendDetailsDto> searchFriends(String userId, SearchVo searchVo);

    boolean isFriendIgnoreSpecial(String userId, String toId);

    List<FriendListDto> getFriendList(String userId);

    boolean isFriend(String userId, String friendId);

    FriendDetailsDto getFriendDetails(String userId, String friendId);

    boolean agreeFriendApply(String userId, AgreeFriendApplyVo agreeFriendApplyVo);

    boolean agreeFriendApply(String userId,String fromId);

    boolean rejectFriendApply(String userId, String fromId);

    boolean addFriendApply(String userId, String targetId);

    boolean setRemark(String userId, SetRemarkVo setRemarkVo);

    boolean setGroup(String userId, SetGroupVo setGroupVo);

    boolean deleteFriend(String userId, DeleteFriendVo deleteFriendVo);

    boolean careForFriend(String userId, CareForFriendVo careForFriendVo);

    boolean unCareForFriend(String userId, UnCareForFriendVo unCareForFriendVo);

    List<Friend> getFriendListFlat(String userId, String friendInfo);

    List<Friend> getFriendListFlatUnread(String userId, String friendInfo);

}
