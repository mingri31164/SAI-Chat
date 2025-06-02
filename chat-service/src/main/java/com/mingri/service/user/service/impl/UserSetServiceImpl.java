package com.mingri.service.user.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.user.repo.vo.dto.SetsDto;
import com.mingri.service.user.repo.vo.req.UpdateUserSetReq;
import com.mingri.service.user.repo.vo.entity.UserSet;
import com.mingri.service.user.repo.mapper.UserSetMapper;
import com.mingri.service.user.service.UserSetService;
import com.mingri.toolkit.SnowflakeIdUtil;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

/**
 * 用户设置表 服务实现类
 */
@Service
public class UserSetServiceImpl extends ServiceImpl<UserSetMapper, UserSet> implements UserSetService {

    @Override
    public UserSet getUserSet(String userId) {
        LambdaQueryWrapper<UserSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSet::getUserId, userId);
        UserSet userSet = getOne(queryWrapper);
        if (null == userSet) {
            userSet = new UserSet();
            userSet.setId(SnowflakeIdUtil.nextIdStr());
            userSet.setUserId(userId);
            userSet.setSets(SetsDto.defaultSets());
            save(userSet);
        }
        return userSet;
    }

    @Override
    public boolean updateUserSet(String userId, UpdateUserSetReq updateUserSetReq) {
        LambdaQueryWrapper<UserSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSet::getUserId, userId);
        UserSet userSet = getOne(queryWrapper);
        if (userSet == null) {
            return false;
        }
        SetsDto sets = userSet.getSets();
        try {
            Field field = SetsDto.class.getDeclaredField(updateUserSetReq.getKey());
            field.setAccessible(true);
            field.set(sets, updateUserSetReq.getValue());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return updateById(userSet);
    }
}
