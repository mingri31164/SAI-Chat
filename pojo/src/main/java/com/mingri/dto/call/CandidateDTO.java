package com.mingri.dto.call;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="WebRTC中的候选者对象")
public class CandidateDTO {

    @ApiModelProperty(value = "候选者的用户ID")
    private String userId;

    @ApiModelProperty(value = "网络候选者的信息")
    private Object candidate;

}
