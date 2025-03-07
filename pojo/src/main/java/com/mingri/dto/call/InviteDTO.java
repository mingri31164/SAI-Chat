package com.mingri.dto.call;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="邀请通话对象")
public class InviteDTO {

    @ApiModelProperty(value = "邀请目标的用户id")
    private String userId;

    @ApiModelProperty(value = "是否仅音频")
    private boolean isOnlyAudio;

}
