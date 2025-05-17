package com.mingri.service.user.repo.req;

import lombok.Data;



@Data
public class UpdatePasswordVo {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
