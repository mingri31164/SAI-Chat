package com.mingri.model.vo.admin.req.conversation;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteConversationReq {
    @NotNull(message = "会话不能为空~")
    private String conversationId;
}
