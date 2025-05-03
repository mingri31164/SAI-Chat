package com.mingri.dto.chatList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="是否已读对象")
public class ReadDTO {

    @ApiModelProperty(value = "私聊对象的ID")
    private String targetId;
}
