package com.mingri.pojo.vo.chatGroup;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class UpdateChatGroupVo {

    @NotNull(message = "群不能为空~")
    private String groupId;
    private String updateKey;
    private String updateValue;

}
