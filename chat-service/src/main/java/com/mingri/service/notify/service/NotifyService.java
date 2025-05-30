package com.mingri.service.notify.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.vo.friend.dto.FriendNotifyDto;
import com.mingri.service.notify.repo.vo.dto.SystemNotifyDto;
import com.mingri.service.notify.repo.vo.req.FriendApplyNotifyReq;
import com.mingri.service.notify.repo.vo.req.ReadNotifyReq;
import com.mingri.service.notify.repo.vo.entity.Notify;
import com.mingri.service.notify.repo.vo.req.DeleteNotifyReq;

import java.util.List;


/**
 * 通知 服务类
 */
public interface NotifyService extends IService<Notify> {

    int unread(String userId);

    int unreadByType(String userId, String type);

    boolean friendApplyNotify(String userId, FriendApplyNotifyReq friendApplyNotifyReq);

    List<FriendNotifyDto> friendListNotify(String userId);

    boolean readNotify(String userId, ReadNotifyReq readNotifyReq);

    List<SystemNotifyDto> SystemListNotify(String userId);

    boolean deleteNotify(DeleteNotifyReq deleteNotifyReq);

    boolean createNotify(String url, String title, String text);


}
