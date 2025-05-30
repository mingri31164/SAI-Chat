package com.mingri.service.notify.repo.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FriendApplyNotifyReq {

    @NotNull(message = "用户不能为空")
    private String userId;

    private String content;
}
