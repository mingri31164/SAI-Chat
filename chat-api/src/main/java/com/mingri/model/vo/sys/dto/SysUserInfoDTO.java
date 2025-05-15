package com.mingri.model.vo.sys.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mingri.model.enumeration.UserTypes;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "用户登录返回的数据格式")
public class SysUserInfoDTO implements Serializable {

    @Schema (description = "主键值")
    @TableField(value = "id")
    private String id;

    @Schema (description = "用户名")
    @TableField(value = "user_name")
    private String name;

    @Schema (description = "头像")
    private String avatar;

    @Schema (description = "用户类型（0管理员，1普通用户，2机器人）")
    @TableField(value = "user_type")
    private UserTypes type;

    @Schema (description = "用户荣誉")
    private List<String> badge;

}
