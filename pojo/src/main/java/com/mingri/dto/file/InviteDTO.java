package com.mingri.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="文件传输邀请对象")
public class InviteDTO {

    @ApiModelProperty(value = "文件传输目标的用户id")
    private String userId;

    @ApiModelProperty(value = "传输的文件")
    private FileInfo fileInfo;

    @Data
    public static class FileInfo {

        @ApiModelProperty(value = "文件名")
        private String name;
        @ApiModelProperty(value = "文件大小")
        private long size;
    }
}
