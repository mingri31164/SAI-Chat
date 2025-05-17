package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.entity.Friend;
import com.mingri.service.chat.repo.req.SearchVo;

import java.util.List;

/**
 * 好友表 服务类
 */
public interface FriendService extends IService<Friend> {


    boolean updateGroupId(String userId, String oldGroupId, String newGroupId);

    List<FriendDetailsDto> searchFriends(String userId, SearchVo searchVo);

    boolean isFriendIgnoreSpecial(String userId, String toId);

}
