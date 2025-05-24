package com.mingri.model.vo.admin.req.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CancelAdminVo {
    @NotNull(message = "用户不能为空")
    private String userId;
}
