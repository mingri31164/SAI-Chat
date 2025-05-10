package com.mingri.pojo.dto.file;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件传输取消对象")
public class CancelDTO {

    @Schema(description = "文件传输对方的用户id")
    private String userId;
}
