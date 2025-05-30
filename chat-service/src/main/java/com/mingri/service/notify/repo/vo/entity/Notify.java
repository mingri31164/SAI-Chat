package com.mingri.service.notify.repo.vo.entity;

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
 * 通知
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="通知表")
@TableName("notify")
public class Notify implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 发送方
     */
    @TableField("from_id")
    private String fromId;

    /**
     * 目标方
     */
    @TableField("to_id")
    private String toId;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 未读方
     */
    @TableField("unread_id")
    private String unreadId;

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
}
