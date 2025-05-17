package com.mingri.service.chat.repo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateChatListVo {
    //好友
    @NotNull(message = "好友id不能为空")
    private String toId;
    private String type = "user";
}
