package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.vo.friend.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.vo.friend.dto.FriendListDto;
import com.mingri.service.chat.repo.vo.friend.entity.Friend;
import com.mingri.service.chat.repo.vo.friend.req.*;

import java.util.List;

/**
 * 好友表 服务类
 */
public interface FriendService extends IService<Friend> {


    boolean updateGroupId(String userId, String oldGroupId, String newGroupId);

    List<FriendDetailsDto> searchFriends(String userId, SearchReq searchReq);

    boolean isFriendIgnoreSpecial(String userId, String toId);

    List<FriendListDto> getFriendList(String userId);

    boolean isFriend(String userId, String friendId);

    FriendDetailsDto getFriendDetails(String userId, String friendId);

    boolean agreeFriendApply(String userId, AgreeFriendApplyReq agreeFriendApplyVo);

    boolean agreeFriendApply(String userId,String fromId);

    boolean rejectFriendApply(String userId, String fromId);

    boolean addFriendApply(String userId, String targetId);

    boolean setRemark(String userId, SetRemarkReq setRemarkVo);

    boolean setGroup(String userId, SetGroupReq setGroupVo);

    boolean deleteFriend(String userId, DeleteFriendReq deleteFriendVo);

    boolean careForFriend(String userId, CareForFriendReq careForFriendVo);

    boolean unCareForFriend(String userId, UnCareForFriendReq unCareForFriendReq);

    List<Friend> getFriendListFlat(String userId, String friendInfo);

    List<Friend> getFriendListFlatUnread(String userId, String friendInfo);

}
