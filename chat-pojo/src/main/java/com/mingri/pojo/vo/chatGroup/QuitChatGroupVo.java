package com.mingri.pojo.vo.chatGroup;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class QuitChatGroupVo {

    @NotNull(message = "群不能为空~")
    private String groupId;

}
