package com.mingri.service.chat.repo.vo.chatgroup.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateChatGroupNameReq {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "群名称不能为空~")
    private String name;
}
