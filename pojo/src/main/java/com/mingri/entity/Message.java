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
@TableName(value = "message",autoResultMap = true)
@ApiModel(value="Message对象", description="")
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

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
