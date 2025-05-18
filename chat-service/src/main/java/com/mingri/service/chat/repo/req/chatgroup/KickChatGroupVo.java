package com.mingri.service.chat.repo.req.chatgroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class KickChatGroupVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "用户不能为空~")
    private String userId;
}
