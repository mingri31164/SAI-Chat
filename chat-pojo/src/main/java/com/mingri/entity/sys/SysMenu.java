package com.mingri.entity.sys;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_menu")
@Schema( description="权限表")
public class SysMenu implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "菜单名")
    private String menuName;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "菜单状态（0显示 1隐藏）")
    private String visible;

    @Schema(description = "菜单状态（0正常 1停用）")
    private String status;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "菜单图标")
    private String icon;

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

    @Schema(description = "是否删除（0未删除 1已删除）")
    private Integer delFlag;

    @Schema(description = "备注")
    private String remark;


}
