package com.mingri.service.chat.repo.vo.group.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateGroupReq {

    @NotNull(message = "分组名称不能为空")
    private String groupName;
}
