package com.mingri.pojo.dto.file;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件传输候选对象")
public class CandidateDTO {

    @Schema(description = "文件传输对方的用户id")
    private String userId;

    @Schema(description = "文件传输候选对象的类型")
    private Object candidate;
}
