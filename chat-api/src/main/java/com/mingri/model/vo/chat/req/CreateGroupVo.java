package com.mingri.model.vo.chat.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateGroupVo {

    @NotNull(message = "分组名称不能为空")
    private String groupName;
}
