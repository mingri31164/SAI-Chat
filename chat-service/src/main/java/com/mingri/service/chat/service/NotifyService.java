package com.mingri.service.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.entity.Notify;


/**
 * 通知 服务类
 */
public interface NotifyService extends IService<Notify> {

    int unread(String userId);

    int unreadByType(String userId, String type);


}
