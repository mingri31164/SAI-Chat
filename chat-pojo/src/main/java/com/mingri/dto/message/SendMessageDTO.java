package com.mingri.dto.message;

import com.mingri.constant.type.MessageType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "发送消息对象")
public class SendMessageDTO {

    @ApiModelProperty(value = "目标用户ID")
    @NotBlank(message = "目标用户不能为空~")
    private String targetId;

    @ApiModelProperty(value = "消息来源（group/user）")
    private String source;

    @ApiModelProperty(value = "消息类型")
    private String type = MessageType.Text;

    @ApiModelProperty(value = "消息内容")
    @NotBlank(message = "消息内容不能为空~")
    private String msgContent;

    @ApiModelProperty(value = "引用的消息ID，非引用消息则为null")
    private String referenceMsgId;
}
