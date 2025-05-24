package com.mingri.model.vo.chat.friend.req;

import lombok.Data;

import javax.validation.constraints.NotNull;




@Data
public class RejectFriendApplyReq {

    @NotNull(message = "fromId不能为空")
    private String fromId;

}
