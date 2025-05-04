package com.mingri.dto.message;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文本消息内容对象")
public class TextMessageContent {

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "消息内容")
    private String content;
}
