package com.mingri.service.chat.repo.req.group;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteGroupVo {
    @NotNull(message = "分组名称分组id")
    private String groupId;
}
