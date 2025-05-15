package com.mingri.model.vo.chat.message.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "消息通知对象")
public class NotifyDto {

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息时间")
    private Date time;

    @Schema(description = "扩展字段")
    private String ext;
}
