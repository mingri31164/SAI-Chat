package com.mingri.service.admin.repo.vo.req.conversation;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResetSecretReq {
    @NotNull(message = "会话不能为空~")
    private String conversationId;
}
