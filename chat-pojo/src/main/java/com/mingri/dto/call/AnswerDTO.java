package com.mingri.dto.call;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="WebRTC中的响应对象")
public class AnswerDTO {

    @ApiModelProperty(value = "响应者的用户id")
    private String userId;

    @ApiModelProperty(value = "表示从 pc（通常是一个RTCPeerConnection对象）中获取的本地SDP描述，包含了关于本地媒体流的信息")
    private Object desc;

}
