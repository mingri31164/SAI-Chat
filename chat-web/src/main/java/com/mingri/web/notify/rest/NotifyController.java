package com.mingri.web.notify.rest;


import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.dto.FriendNotifyDto;
import com.mingri.service.chat.repo.dto.SystemNotifyDto;
import com.mingri.service.chat.repo.req.notify.FriendApplyNotifyVo;
import com.mingri.service.chat.repo.req.notify.ReadNotifyVo;
import com.mingri.service.notify.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/v1/api/notify")
@Slf4j
public class NotifyController {

    @Resource
    NotifyService notifyService;

    /**
     * 好有通知列表
     *
     * @return
     */
    @GetMapping("/friend/list")
    public JSONObject friendListNotify(@Userid String userId) {
        List<FriendNotifyDto> result = notifyService.friendListNotify(userId);
        return ResultUtil.Succeed(result);
    }


    /**
     * 好有申请通知
     *
     * @return
     */
    @PostMapping("/friend/apply")
    public JSONObject friendApplyNotify(@Userid String userId, @RequestBody FriendApplyNotifyVo friendApplyNotifyVo) {
        boolean result = notifyService.friendApplyNotify(userId, friendApplyNotifyVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 通知已读
     *
     * @return
     */
    @PostMapping("/read")
    public JSONObject readNotify(@Userid String userId, @RequestBody ReadNotifyVo readNotifyVo) {
        boolean result = notifyService.readNotify(userId, readNotifyVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 系统通知列表
     *
     * @return
     */
    @GetMapping("/system/list")
    public JSONObject SystemListNotify(@Userid String userId) {
        List<SystemNotifyDto> result = notifyService.SystemListNotify(userId);
        return ResultUtil.Succeed(result);
    }
}

