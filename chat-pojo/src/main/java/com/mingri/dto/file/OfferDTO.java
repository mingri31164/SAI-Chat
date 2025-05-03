package com.mingri.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="WebRTC中的发起对象")
public class OfferDTO {

    @ApiModelProperty(value = "文件传输目标的用户id")
    private String userId;

    @ApiModelProperty(value = "表示从 pc（通常是一个RTCPeerConnection对象）中获取的本地SDP描述，包含了关于本地媒体流的信息")
    private Object desc;
}
