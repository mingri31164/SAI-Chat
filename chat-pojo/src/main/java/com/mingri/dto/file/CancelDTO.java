package com.mingri.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="文件传输取消对象")
public class CancelDTO {

    @ApiModelProperty(value = "文件传输对方的用户id")
    private String userId;
}
