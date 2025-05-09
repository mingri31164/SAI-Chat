package com.mingri.entity.sys;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.List;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mingri.enumeration.UserStatus;
import com.mingri.enumeration.UserTypes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "sys_user",autoResultMap = true)
@Schema(description="用户表")
public class SysUser implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "账号状态（0正常 1停用）")
    private UserStatus status;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户性别（0男，1女，2未知）")
    private Integer sex;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户荣誉")
    @TableField(value = "badge", typeHandler = JacksonTypeHandler.class)
    private List<String> badge;

    @Schema(description = "用户类型（0管理员，1普通用户，2机器人）")
    private UserTypes userType;

    @Schema(description = "用户最新登录时间")
    @TableField(value = "login_time",fill = FieldFill.UPDATE)
    private Date loginTime;

    @Schema(description = "创建人的用户id")
    @TableField(value = "create_By",fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新人")
    @TableField(value = "update_By",fill = FieldFill.UPDATE)
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Schema(description = "删除标志（0代表未删除，1代表已删除）")
    private Integer delFlag;


}
