package com.mingri.vo.chatGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class KickChatGroupVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "用户不能为空~")
    private String userId;
}
