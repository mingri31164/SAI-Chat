package com.mingri.service.chat.repo.vo.chatgroup.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateChatGroupReq {
    @NotNull(message = "群不能为空~")
    private String groupId;
    private String updateKey;
    private String updateValue;
}
