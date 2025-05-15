package com.mingri.model.vo.chat.file.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接受文件传输对象")
public class AcceptDTO {

    @Schema(description = "发起文件传输的用户ID")
    private String userId;
}
