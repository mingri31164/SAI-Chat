package com.mingri.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 聊天群成员表
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_group_member")
@ApiModel(value="ChatGroupMember对象", description="聊天群成员表")
public class ChatGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty(value = "聊天群id")
    @TableField("chat_group_id")
    private String chatGroupId;

    @ApiModelProperty(value = "成员id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "群备注")
    @TableField("group_remark")
    private String groupRemark;

    @ApiModelProperty(value = "群昵称")
    @TableField("group_name")
    private String groupName;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "聊天背景")
    @TableField("chat_background")
    private String chatBackground;


}
