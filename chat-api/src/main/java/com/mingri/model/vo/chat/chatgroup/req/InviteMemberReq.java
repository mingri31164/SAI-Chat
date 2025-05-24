package com.mingri.model.vo.chat.chatgroup.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InviteMemberReq {
    @NotNull(message = "群不能为空~")
    private String groupId;
    private String[] userIds;
}
