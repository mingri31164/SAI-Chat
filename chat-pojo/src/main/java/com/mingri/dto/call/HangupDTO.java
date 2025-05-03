package com.mingri.dto.call;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="挂断通话对象")
public class HangupDTO {

    @ApiModelProperty(value = "通话对方的用户id")
    private String userId;
}
