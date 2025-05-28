package com.mingri.web.notify.rest;


import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.chat.friend.dto.FriendNotifyDto;
import com.mingri.model.vo.notify.dto.SystemNotifyDto;
import com.mingri.model.vo.notify.req.FriendApplyNotifyReq;
import com.mingri.model.vo.notify.req.ReadNotifyReq;
import com.mingri.service.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag(name = "通知基础接口")
@RequestMapping("/v1/api/notify")
public class NotifyController {

    @Resource
    NotifyService notifyService;

    /**
     * 好有通知列表
     */
    @GetMapping("/friend/list")
    @Operation(summary = "获取好友通知列表")
    public JSONObject friendListNotify(@Userid String userId) {
        List<FriendNotifyDto> result = notifyService.friendListNotify(userId);
        return ResultUtil.Succeed(result);
    }


    /**
     * 好有申请通知
     */
    @PostMapping("/friend/apply")
    @Operation(summary = "获取好友申请通知")
    public JSONObject friendApplyNotify(@Userid String userId, @RequestBody FriendApplyNotifyReq friendApplyNotifyReq) {
        boolean result = notifyService.friendApplyNotify(userId, friendApplyNotifyReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 通知已读
     */
    @PostMapping("/read")
    @Operation(summary = "设置通知已读")
    public JSONObject readNotify(@Userid String userId, @RequestBody ReadNotifyReq readNotifyReq) {
        boolean result = notifyService.readNotify(userId, readNotifyReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 系统通知列表
     */
    @GetMapping("/system/list")
    @Operation(summary = "获取系统通知列表")
    public JSONObject SystemListNotify(@Userid String userId) {
        List<SystemNotifyDto> result = notifyService.SystemListNotify(userId);
        return ResultUtil.Succeed(result);
    }
}

