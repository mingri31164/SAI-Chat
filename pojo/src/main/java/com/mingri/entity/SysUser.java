package com.mingri.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.List;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mingri.enumeration.UserStatus;
import com.mingri.enumeration.UserTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "sys_user",autoResultMap = true)
@ApiModel(value="SysUser对象", description="用户表")
public class SysUser implements Serializable {

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "账号状态（0正常 1停用）")
    private UserStatus status;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "用户性别（0男，1女，2未知）")
    private Integer sex;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty("用户荣誉")
    @TableField(value = "badge", typeHandler = JacksonTypeHandler.class)
    private List<String> badge;

    @ApiModelProperty(value = "用户类型（0管理员，1普通用户，2机器人）")
    private UserTypes userType;

    @ApiModelProperty(value = "用户最新登录时间")
    @TableField(value = "login_time",fill = FieldFill.UPDATE)
    private Date loginTime;

    @ApiModelProperty(value = "创建人的用户id")
    @TableField(value = "create_By",fill = FieldFill.INSERT)
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    @TableField(value = "update_By",fill = FieldFill.UPDATE)
    private Long updateBy;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "删除标志（0代表未删除，1代表已删除）")
    private Integer delFlag;


}
