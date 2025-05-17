package com.mingri.service.user.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.vo.user.dto.SetsDto;
import com.mingri.model.vo.user.req.UpdateUserSetVo;
import com.mingri.service.user.repo.entity.UserSet;
import com.mingri.service.user.repo.mapper.UserSetMapper;
import com.mingri.service.user.service.UserSetService;
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
            userSet.setId(IdUtil.randomUUID());
            userSet.setUserId(userId);
            userSet.setSets(SetsDto.defaultSets());
            save(userSet);
        }
        return userSet;
    }

    @Override
    public boolean updateUserSet(String userId, UpdateUserSetVo updateUserSetVo) {
        LambdaQueryWrapper<UserSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSet::getUserId, userId);
        UserSet userSet = getOne(queryWrapper);
        if (userSet == null) {
            return false;
        }
        SetsDto sets = userSet.getSets();
        try {
            Field field = SetsDto.class.getDeclaredField(updateUserSetVo.getKey());
            field.setAccessible(true);
            field.set(sets, updateUserSetVo.getValue());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return updateById(userSet);
    }
}
