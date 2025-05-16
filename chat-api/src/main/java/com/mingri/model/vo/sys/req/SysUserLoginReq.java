package com.mingri.model.vo.sys.req;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
@Schema(description = "用户登录对象")
public class SysUserLoginReq implements Serializable {

    @NotBlank(message = "用户名不能为空~")
    @Schema(description = "用户名")
    private String userName;

    @NotBlank(message = "密码不能为空")
//    @Size(min = 6, message = "密码长度必须至少为 6 位")
    @Schema(description = "密码")
    private String password;

}
