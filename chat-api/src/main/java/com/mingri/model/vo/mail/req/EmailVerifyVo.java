package com.mingri.model.vo.mail.req;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class EmailVerifyVo {
    @Email(message = "邮箱格式有误~")
    private String email;
}
