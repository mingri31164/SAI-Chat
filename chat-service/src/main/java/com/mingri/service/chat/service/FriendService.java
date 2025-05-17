package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.chat.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.entity.Friend;

import java.util.List;

/**
 * 好友表 服务类
 */
public interface FriendService extends IService<Friend> {


    boolean updateGroupId(String userId, String oldGroupId, String newGroupId);

}
