package com.mingri.model.vo.chat.message.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
@Schema(description = "消息记录对象")
public class RecordDTO {

    @Schema(description = "目标用户id")
    private String targetId;

    @Schema(description = "消息起始下标")
    private int index;

    @Schema(description = "消息数量")
    @Max(100)
    private int num;
}
