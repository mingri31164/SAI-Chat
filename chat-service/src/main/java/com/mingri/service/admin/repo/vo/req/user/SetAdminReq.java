package com.mingri.service.admin.repo.vo.req.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetAdminReq {
    @NotNull(message = "用户不能为空")
    private String userId;
}
