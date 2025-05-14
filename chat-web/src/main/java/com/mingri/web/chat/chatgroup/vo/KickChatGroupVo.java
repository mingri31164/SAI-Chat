package com.mingri.web.chat.chatgroup.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class KickChatGroupVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "用户不能为空~")
    private String userId;
}
