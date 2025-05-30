package com.mingri.service.chat.repo.vo.chatgroup.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天群公告表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="聊天群通知表")
@TableName("chat_group_notice")
public class ChatGroupNotice implements Serializable {

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
     * 公告内容
     */
    @TableField("notice_content")
    private String noticeContent;

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

    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String portrait;
}
