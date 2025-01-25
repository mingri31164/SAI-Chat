package com.mingri.dto.message;

import com.mingri.constant.MessageType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SendMessageDTO {
    @NotBlank(message = "目标用户不能为空~")
    private String targetId;
    private String source;
    private String type = MessageType.Text;
    @NotBlank(message = "消息内容不能为空~")
    private String msgContent;
    private String referenceMsgId;
}
