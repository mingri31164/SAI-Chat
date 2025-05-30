package com.mingri.service.chat.repo.vo.chatgroup.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DissolveChatGroupReq {
    @NotNull(message = "群不能为空~")
    private String groupId;
}
