package com.mingri.service.chat.repo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;


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
@Schema(description="聊天群表")
public class ChatGroupDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "群头像")
    @TableField("avatar")
    private String avatar;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "创建用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "群主id")
    @TableField("owner_user_id")
    private String ownerUserId;

    @Schema(description = "群名名称")
    @TableField("name")
    private String name;

    @Schema(description = "群公告")
    @TableField("notice")
    private String notice;

    @Schema(description = "成员数")
    @TableField("member_num")
    private Integer memberNum;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Schema(description = "群号")
    @TableField("chat_group_number")
    private String chatGroupNumber;


}
