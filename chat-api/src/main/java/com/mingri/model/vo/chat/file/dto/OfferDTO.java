package com.mingri.model.vo.chat.file.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "WebRTC中的发起对象")
public class OfferDTO {

    @Schema(description = "文件传输目标的用户id")
    private String userId;

    @Schema(description = "表示从 pc（通常是一个RTCPeerConnection对象）中获取的本地SDP描述，包含了关于本地媒体流的信息")
    private Object desc;
}
