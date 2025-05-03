package com.mingri.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel(value = "修改密码对象")
public class PasswordEditDTO implements Serializable {

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "旧密码")
    private String oldPassword;

    @ApiModelProperty(value = "新密码")
    private String newPassword;

}
