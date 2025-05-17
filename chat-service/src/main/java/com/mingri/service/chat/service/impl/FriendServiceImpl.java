package com.mingri.service.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.constant.UserRole;
import com.mingri.service.chat.repo.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.entity.Friend;
import com.mingri.service.chat.repo.mapper.FriendMapper;
import com.mingri.service.chat.repo.req.SearchVo;
import com.mingri.service.chat.service.FriendService;
import com.mingri.service.user.repo.entity.User;
import com.mingri.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Override
    public boolean updateGroupId(String userId, String oldGroupId, String newGroupId) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getGroupId, newGroupId)
                .eq(Friend::getGroupId, oldGroupId)
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public List<FriendDetailsDto> searchFriends(String userId, SearchVo searchVo) {
        return friendMapper.searchFriends(userId, "%" + searchVo.getSearchInfo() + "%");
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
}
