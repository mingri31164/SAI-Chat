package com.mingri.model.vo.user.req.login;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class LoginReq {
    @NotNull(message = "账号不能为空")
    private String account;
    @NotNull(message = "密码不能为空")
    private String password;

    // 登录设备
    private String onlineEquipment;
}
