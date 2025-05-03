package com.mingri.dto.chatList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="删除私聊对象")
public class DeleteDTO {

    @ApiModelProperty(value = "私聊对象的ID")
    private String chatListId;
}
