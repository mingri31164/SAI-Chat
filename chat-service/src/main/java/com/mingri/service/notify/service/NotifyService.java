package com.mingri.service.notify.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.chat.friend.dto.FriendNotifyDto;
import com.mingri.model.vo.notify.dto.SystemNotifyDto;
import com.mingri.model.vo.notify.req.FriendApplyNotifyReq;
import com.mingri.model.vo.notify.req.ReadNotifyReq;
import com.mingri.model.vo.notify.entity.Notify;
import com.mingri.model.vo.notify.req.DeleteNotifyReq;

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
