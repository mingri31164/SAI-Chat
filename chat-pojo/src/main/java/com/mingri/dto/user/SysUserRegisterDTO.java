package com.mingri.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel(description = "用户注册对象")
public class SysUserRegisterDTO implements Serializable {

    @NotBlank(message = "用户名不能为空~")
    @ApiModelProperty("用户名")
    private String userName;

    @NotBlank(message = "密码不能为空")
//    @Size(min = 6, message = "密码长度必须至少为 6 位")
    @ApiModelProperty("密码")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @ApiModelProperty("邮箱")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @ApiModelProperty("邮箱验证码")
    private String emailCode;



}
