package com.mingri.pojo.dto.call;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "WebRTC中的候选者对象")
public class CandidateDTO {

    @Schema(description = "候选者的用户ID")
    private String userId;

    @Schema(description = "网络候选者的信息")
    private Object candidate;

}
