package com.mingri.service.chat.repo.req.chatlist;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteChatListVo {
    @NotNull(message = "会话id不能为空")
    private String chatListId;
}
