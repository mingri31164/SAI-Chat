package com.mingri.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserDTO implements Serializable {

    private Long id;

    private String nickName;

    private String userName;

    private String password;

    private String phone;

    private String email;

    private Integer sex;

}
