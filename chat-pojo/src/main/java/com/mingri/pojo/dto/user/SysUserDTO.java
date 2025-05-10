package com.mingri.pojo.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

@Data
@Schema(description = "用户信息对象")
public class SysUserDTO implements Serializable {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户性别（未定）")
    private Integer sex;

}
