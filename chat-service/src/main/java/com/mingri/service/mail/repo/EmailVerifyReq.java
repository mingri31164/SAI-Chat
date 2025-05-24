package com.mingri.service.mail.repo;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class EmailVerifyReq {
    @Email(message = "邮箱格式有误~")
    private String email;
}
