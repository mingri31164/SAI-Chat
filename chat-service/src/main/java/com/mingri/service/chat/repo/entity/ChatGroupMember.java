package com.mingri.service.chat.repo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天群成员表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_group_member")
public class ChatGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 聊天群id
     */
    @TableField("chat_group_id")
    private String chatGroupId;

    /**
     * 成员id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 群备注
     */
    @TableField("group_remark")
    private String groupRemark;

    /**
     * 群昵称
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 聊天背景
     */
    @TableField("chat_background")
    private String chatBackground;

}
