package com.mingri.dto.chatList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ApiModel(value="创建私聊对象")
public class CreateDTO {

    @ApiModelProperty(value = "目标用户id")
    @NotBlank(message = "目标不能为空~")
    private String targetId;
}
