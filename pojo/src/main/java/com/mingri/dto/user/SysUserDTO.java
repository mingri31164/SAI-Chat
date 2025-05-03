package com.mingri.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel(value = "用户信息对象")
public class SysUserDTO implements Serializable {

    @ApiModelProperty(value = "用户ID")
    private String id;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "用户密码")
    private String password;

    @ApiModelProperty(value = "用户手机号")
    private String phone;

    @ApiModelProperty(value = "用户邮箱")
    private String email;

    @ApiModelProperty(value = "用户性别（未定）")
    private Integer sex;

}
