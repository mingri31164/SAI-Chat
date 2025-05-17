package com.mingri.service.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.entity.Friend;
import com.mingri.service.chat.repo.mapper.FriendMapper;
import com.mingri.service.chat.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * 好友表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Override
    public boolean updateGroupId(String userId, String oldGroupId, String newGroupId) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getGroupId, newGroupId)
                .eq(Friend::getGroupId, oldGroupId)
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }
}
