package com.mingri.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Max;

@Data
@ApiModel(value = "消息记录对象")
public class RecordDTO {

    @ApiModelProperty(value = "目标用户id")
    private String targetId;

    @ApiModelProperty(value = "消息起始下标")
    private int index;

    @ApiModelProperty(value = "消息数量")
    @Max(100)
    private int num;
}
