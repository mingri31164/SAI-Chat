package com.mingri.service.chat.repo.req.chatgroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InviteMemberVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    private String[] userIds;
}
