package com.mingri.vo.chatGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QuitChatGroupVo {

    @NotNull(message = "群不能为空~")
    private String groupId;

}
