package com.mingri.model.vo.chat.friend.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetGroupReq {
    @NotNull(message = "好友不能为空")
    private String friendId;
    private String groupId;
}
