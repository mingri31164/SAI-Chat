package com.mingri.model.vo.sys.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

@Data
@Schema(description = "修改密码对象")
public class PasswordEditDTO implements Serializable {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "旧密码")
    private String oldPassword;

    @Schema(description = "新密码")
    private String newPassword;

}
