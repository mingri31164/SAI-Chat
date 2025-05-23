package com.mingri.service.chat.repo.req.notify;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FriendApplyNotifyVo {

    @NotNull(message = "用户不能为空")
    private String userId;

    private String content;
}
