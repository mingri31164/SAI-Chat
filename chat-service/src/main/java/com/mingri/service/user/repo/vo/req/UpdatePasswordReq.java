package com.mingri.service.user.repo.vo.req;

import lombok.Data;



@Data
public class UpdatePasswordReq {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
