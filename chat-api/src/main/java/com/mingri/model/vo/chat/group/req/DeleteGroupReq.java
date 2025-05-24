package com.mingri.model.vo.chat.group.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteGroupReq {
    @NotNull(message = "分组名称分组id")
    private String groupId;
}
