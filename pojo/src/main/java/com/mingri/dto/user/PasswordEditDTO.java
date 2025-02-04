package com.mingri.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

    //用户id
    private Long userId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

}
