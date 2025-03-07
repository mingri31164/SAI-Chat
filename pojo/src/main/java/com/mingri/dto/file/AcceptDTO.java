package com.mingri.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="接受文件传输对象")
public class AcceptDTO {

    @ApiModelProperty(value = "发起文件传输的用户ID")
    private String userId;
}
