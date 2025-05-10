package com.mingri.pojo.vo.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mingri.common.enumeration.UserTypes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录返回的数据格式")
public class SysUserLoginVO {

    @TableField("id")
    @Schema (description = "主键值")
    private String userId;

    @Schema (description = "用户名")
    private String userName;

    @Schema (description = "邮箱")
    private String email;

    @Schema (description = "头像")
    private String avatar;

    @TableField("user_type")
    @Schema (description = "用户类型（0管理员，1普通用户，2机器人）")
    private UserTypes type;

    @Schema (description = "jwt令牌")
    private String token;

}
