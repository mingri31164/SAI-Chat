package com.mingri.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel(value = "消息通知对象")
public class NotifyDto {

    @ApiModelProperty(value = "消息类型")
    private String type;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息时间")
    private Date time;

    @ApiModelProperty(value = "扩展字段")
    private String ext;
}
