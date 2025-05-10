package com.mingri.pojo.entity.chat;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mingri.pojo.vo.sys.SysUserInfoVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "message",autoResultMap = true)
@Schema(description = "Message对象")
public class Message implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    private String fromId;

    private String toId;

    @TableField(value = "from_info", typeHandler = JacksonTypeHandler.class)
    private SysUserInfoVO fromInfo;

    private String message;

    @TableField(value = "reference_msg", typeHandler = JacksonTypeHandler.class)
    private Message referenceMsg;

    @TableField(value = "at_user", typeHandler = JacksonTypeHandler.class)
    private SysUserInfoVO atUser;

    private Boolean isShowTime;

    private String type;

    private String source;

    @Schema(description = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
