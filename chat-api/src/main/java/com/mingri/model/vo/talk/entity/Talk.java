package com.mingri.model.vo.talk.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mingri.model.vo.talk.dto.TalkContentDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 说说
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="说说管理表")
@TableName(value = "talk", autoResultMap = true)
public class Talk implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 说说内容
     */
    @TableField(value = "content", typeHandler = JacksonTypeHandler.class)
    private TalkContentDto content;

    /**
     * 点赞数量
     */
    @TableField("like_num")
    private Integer likeNum;

    /**
     * 评论数量
     */
    @TableField("comment_num")
    private Integer commentNum;

    /**
     * 最近评论内容
     */
//    @SensitiveField(bind = "content")
    @TableField("latest_comment")
    private String latestComment;

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
