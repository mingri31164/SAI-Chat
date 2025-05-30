package com.mingri.service.chat.repo.vo.friend.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CareForFriendReq {

    @NotNull(message = "好友不能为空")
    private String friendId;
}
