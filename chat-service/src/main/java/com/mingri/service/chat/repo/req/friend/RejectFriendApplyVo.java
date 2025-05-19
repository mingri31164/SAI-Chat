package com.mingri.service.chat.repo.req.friend;

import lombok.Data;

import javax.validation.constraints.NotNull;




@Data
public class RejectFriendApplyVo {

    @NotNull(message = "fromId不能为空")
    private String fromId;

}
