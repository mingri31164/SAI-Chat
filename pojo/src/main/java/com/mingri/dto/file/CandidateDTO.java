package com.mingri.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="文件传输候选对象")
public class CandidateDTO {

    @ApiModelProperty(value = "文件传输对方的用户id")
    private String userId;

    @ApiModelProperty(value = "文件传输候选对象的类型")
    private Object candidate;
}
