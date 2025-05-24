package com.mingri.model.vo.chat.friend.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AgreeFriendApplyReq {

    @NotNull(message = "好友申请的通知id")
    public String notifyId;

    private String fromId;
}
