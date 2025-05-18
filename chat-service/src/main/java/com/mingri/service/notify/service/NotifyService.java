package com.mingri.service.notify.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.notify.repo.entity.Notify;


/**
 * 通知 服务类
 */
public interface NotifyService extends IService<Notify> {

    int unread(String userId);

    int unreadByType(String userId, String type);


}
