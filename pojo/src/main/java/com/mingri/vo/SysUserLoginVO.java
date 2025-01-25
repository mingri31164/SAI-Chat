package com.mingri.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mingri.enumeration.UserTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/24 16:10
 * @ClassName: SysLoginVO
 * @Version: 1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户登录返回的数据格式")
public class SysUserLoginVO {

    @TableField("id")
    @ApiModelProperty("主键值")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("用户类型（0管理员，1普通用户，2机器人）")
    private UserTypes userType;

    @ApiModelProperty("jwt令牌")
    private String token;

}
