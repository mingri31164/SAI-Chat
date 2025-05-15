package com.mingri.service.chat.repo.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import com.mingri.model.vo.sys.dto.SysUserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;



@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "chat_list", autoResultMap = true)
@Schema(description = "ChatList对象")
public class ChatListDO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    private String userId;

    private String targetId;

    @TableField(value = "target_info", typeHandler = JacksonTypeHandler.class)
    private SysUserInfoDTO targetInfo;

    private Integer unreadCount;

    @TableField(value = "last_message", typeHandler = JacksonTypeHandler.class)
    private MessageDO lastMessage;

    private String type;

    @Schema(description = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
