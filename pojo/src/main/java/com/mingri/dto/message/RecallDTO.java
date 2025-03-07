package com.mingri.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "消息撤回对象")
public class RecallDTO {

    @ApiModelProperty(value = "撤回的消息id")
    @NotBlank(message = "消息不能为空~")
    private String msgId;
}
