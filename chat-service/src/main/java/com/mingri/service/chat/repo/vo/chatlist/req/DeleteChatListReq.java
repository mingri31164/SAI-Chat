package com.mingri.service.chat.repo.vo.chatlist.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteChatListReq {
    @NotNull(message = "会话id不能为空")
    private String chatListId;
}
