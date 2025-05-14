package com.mingri.web.chat.chatgroup.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class InviteMemberVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    private String[] userIds;
}
