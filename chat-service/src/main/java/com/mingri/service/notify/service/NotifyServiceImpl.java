package com.mingri.service.notify.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.constant.FriendApplyStatus;
import com.mingri.model.constant.NotifyType;
import com.mingri.model.exception.BaseException;
import com.mingri.model.vo.chat.friend.dto.FriendNotifyDto;
import com.mingri.model.vo.notify.dto.SystemNotifyDto;
import com.mingri.model.vo.notify.req.FriendApplyNotifyReq;
import com.mingri.model.vo.notify.req.ReadNotifyReq;
import com.mingri.service.chat.service.FriendService;
import com.mingri.model.vo.notify.entity.Notify;
import com.mingri.service.notify.repo.mapper.NotifyMapper;
import com.mingri.model.vo.notify.req.DeleteNotifyReq;
import com.mingri.service.websocket.WebSocketService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通知 服务实现类
 */
@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, Notify> implements NotifyService {

    @Resource
    NotifyMapper notifyMapper;
    @Resource
    WebSocketService webSocketService;

    // TODO 后续考虑更好地解决循环依赖问题
    @Lazy
    @Resource
    FriendService friendService;




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

    @Override
    public boolean friendApplyNotify(String userId, FriendApplyNotifyReq friendApplyNotifyReq) {
        if (friendService.isFriend(userId, friendApplyNotifyReq.getUserId())) {
            throw new BaseException("ta已是您的好友");
        }

        LambdaQueryWrapper<Notify> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notify::getFromId, userId)
                .eq(Notify::getToId, friendApplyNotifyReq.getUserId())
                .eq(Notify::getType, NotifyType.Friend_Apply);
        if (count(queryWrapper) > 0) {
            throw new BaseException("请勿重复申请");
        }

        Notify notify = new Notify();
        notify.setId(IdUtil.randomUUID());
        notify.setFromId(userId);
        notify.setToId(friendApplyNotifyReq.getUserId());
        notify.setType(NotifyType.Friend_Apply);
        notify.setStatus(FriendApplyStatus.Wait);
        notify.setContent(friendApplyNotifyReq.getContent());
        notify.setUnreadId(friendApplyNotifyReq.getUserId());
        webSocketService.sendNotifyToUser(notify, friendApplyNotifyReq.getUserId());
        return save(notify);
    }

    @Override
    public List<FriendNotifyDto> friendListNotify(String userId) {
        List<FriendNotifyDto> notifyList = notifyMapper.friendListNotify(userId, NotifyType.Friend_Apply);
        return notifyList;
    }

    @Override
    public boolean readNotify(String userId, ReadNotifyReq readNotifyReq) {
        LambdaUpdateWrapper<Notify> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Notify::getUnreadId, "")
                .eq(Notify::getUnreadId, userId)
                .eq(Notify::getType, readNotifyReq.getNotifyType());
        return update(updateWrapper);
    }

    @Override
    public List<SystemNotifyDto> SystemListNotify(String userId) {
        List<SystemNotifyDto> result = notifyMapper.SystemListNotify();
        return result;
    }

    @Override
    public boolean deleteNotify(DeleteNotifyReq deleteNotifyReq) {
        return removeById(deleteNotifyReq.getNotifyId());
    }

    @Override
    public boolean createNotify(String url, String title, String text) {
        SystemNotifyDto.SystemNotifyContent content = new SystemNotifyDto.SystemNotifyContent();
        content.setImg(url);
        content.setText(text);
        content.setTitle(title);
        Notify notify = new Notify();
        notify.setId(IdUtil.randomUUID());
        notify.setType(NotifyType.System);
        notify.setToId("all");
        notify.setFromId("system");
        notify.setContent(JSONUtil.toJsonStr(content));
        return save(notify);
    }

}
