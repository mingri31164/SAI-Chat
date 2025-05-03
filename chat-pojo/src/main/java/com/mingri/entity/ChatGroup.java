package com.mingri.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 聊天群表
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_group")
@ApiModel(value="ChatGroup对象", description="聊天群表")
public class ChatGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "群头像")
    @TableField("avatar")
    private String avatar;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty(value = "创建用户id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "群主id")
    @TableField("owner_user_id")
    private String ownerUserId;

    @ApiModelProperty(value = "群名名称")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "群公告")
    @TableField("notice")
    private String notice;

    @ApiModelProperty(value = "成员数")
    @TableField("member_num")
    private Integer memberNum;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "群号")
    @TableField("chat_group_number")
    private String chatGroupNumber;


}
