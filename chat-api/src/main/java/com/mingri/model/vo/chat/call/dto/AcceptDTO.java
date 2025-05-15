package com.mingri.model.vo.chat.call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description ="接受通话对象")
public class AcceptDTO {

    @Schema(description = "发起通话的用户id")
    private String userId;

}
