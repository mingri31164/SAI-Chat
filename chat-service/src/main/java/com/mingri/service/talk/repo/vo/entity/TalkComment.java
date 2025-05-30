package com.mingri.service.talk.repo.vo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mingri.core.sensitive.SensitiveField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 说说评论
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="说说评论表")
@TableName("talk_comment")
public class TalkComment implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private String id;

    /**
     * 说说id
     */
    @TableField("talk_id")
    private String talkId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 评论内容
     */
    @SensitiveField
    @TableField("content")
    private String content;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

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
