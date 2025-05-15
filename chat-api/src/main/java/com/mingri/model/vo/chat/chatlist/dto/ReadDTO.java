package com.mingri.model.vo.chat.chatlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "是否已读对象")
public class ReadDTO {

    @Schema(description = "私聊对象的ID")
    private String targetId;
}
