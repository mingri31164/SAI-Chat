package com.mingri.service.notify.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.notify.repo.entity.Notify;
import com.mingri.service.notify.repo.mapper.NotifyMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 通知 服务实现类
 */
@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, Notify> implements NotifyService {

    @Resource
    NotifyMapper notifyMapper;


    @Override
    public int unread(String userId) {
        Integer num = notifyMapper.unreadByUserId(userId);
        return num == null ? 0 : num;
    }

    @Override
    public int unreadByType(String userId, String type) {
        Integer num = notifyMapper.unreadByType(userId, type);
        return num == null ? 0 : num;
    }

}
