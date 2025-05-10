package com.mingri.pojo.dto.message;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "消息撤回对象")
public class RecallDTO {

    @Schema(description = "撤回的消息id")
    @NotBlank(message = "消息不能为空~")
    private String msgId;
}
