package com.mingri.pojo.dto.message;

import com.mingri.api.constant.type.MessageType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Schema(description = "发送消息对象")
public class SendMessageDTO {

    @Schema(description = "目标用户ID")
    @NotBlank(message = "目标用户不能为空~")
    private String targetId;

    @Schema(description = "消息来源（group/user）")
    private String source;

    @Schema(description = "消息类型")
    private String type = MessageType.Text;

    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空~")
//    @SensitiveField(bind = "msgContent")
    private String msgContent;

    @Schema(description = "引用的消息ID，非引用消息则为null")
    private String referenceMsgId;
}
