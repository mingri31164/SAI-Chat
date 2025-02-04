package com.mingri.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mingri.enumeration.UserTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户登录返回的数据格式")
public class SysUserInfoVO implements Serializable {

    @ApiModelProperty("主键值")
    @TableField(value = "id")
    private String id;

    @ApiModelProperty("用户名")
    @TableField(value = "user_name")
    private String name;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("用户类型（0管理员，1普通用户，2机器人）")
    @TableField(value = "user_type")
    private UserTypes type;

    @ApiModelProperty("用户荣誉")
    private List<String> badge;

}
