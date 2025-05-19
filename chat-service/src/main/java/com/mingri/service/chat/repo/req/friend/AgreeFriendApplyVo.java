package com.mingri.service.chat.repo.req.friend;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AgreeFriendApplyVo {

    @NotNull(message = "好友申请的通知id")
    public String notifyId;

    private String fromId;
}
