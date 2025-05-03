package com.mingri.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mingri.vo.SysUserInfoVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;



@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "chat_list", autoResultMap = true)
@ApiModel(value="ChatList对象", description="")
public class ChatList implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    private String userId;

    private String targetId;

    @TableField(value = "target_info", typeHandler = JacksonTypeHandler.class)
    private SysUserInfoVO targetInfo;

    private Integer unreadCount;

    @TableField(value = "last_message", typeHandler = JacksonTypeHandler.class)
    private Message lastMessage;

    private String type;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
