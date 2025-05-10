package com.mingri.pojo.dto.file;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件传输邀请对象")
public class InviteDTO {

    @Schema(description = "文件传输目标的用户id")
    private String userId;

    @Schema(description = "传输的文件")
    private FileInfo fileInfo;

    @Data
    public static class FileInfo {

        @Schema(description = "文件名")
        private String name;
        @Schema(description = "文件大小")
        private long size;
    }
}
