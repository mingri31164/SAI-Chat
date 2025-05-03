package com.mingri.dto.call;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="接受通话对象", description="")
public class AcceptDTO {

    @ApiModelProperty(value = "发起通话的用户id")
    private String userId;

}
