package com.mingri.model.vo.chat.file.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "WebRTC中的响应对象")
public class AnswerDTO {

    @Schema(description = "响应者的用户id")
    private String userId;

    @Schema(description = "表示从 pc（通常是一个RTCPeerConnection对象）中获取的本地SDP描述，包含了关于本地媒体流的信息")
    private Object desc;
}
