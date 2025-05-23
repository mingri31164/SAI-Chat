package com.mingri.service.notify.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.FriendNotifyDto;
import com.mingri.service.chat.repo.dto.SystemNotifyDto;
import com.mingri.service.chat.repo.req.notify.FriendApplyNotifyVo;
import com.mingri.service.chat.repo.req.notify.ReadNotifyVo;
import com.mingri.service.notify.repo.entity.Notify;
import com.mingri.service.notify.repo.req.DeleteNotifyVo;

import java.util.List;


/**
 * 通知 服务类
 */
public interface NotifyService extends IService<Notify> {

    int unread(String userId);

    int unreadByType(String userId, String type);

    boolean friendApplyNotify(String userId, FriendApplyNotifyVo friendApplyNotifyVo);

    List<FriendNotifyDto> friendListNotify(String userId);

    boolean readNotify(String userId, ReadNotifyVo readNotifyVo);

    List<SystemNotifyDto> SystemListNotify(String userId);

    boolean deleteNotify(DeleteNotifyVo deleteNotifyVo);

    boolean createNotify(String url, String title, String text);


}
