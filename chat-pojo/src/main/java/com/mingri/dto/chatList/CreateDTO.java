package com.mingri.dto.chatList;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建私聊对象")
public class CreateDTO {

    @Schema(description = "目标用户id")
    @NotBlank(message = "目标不能为空~")
    private String targetId;
}
