package com.mingri.model.vo.chat.call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "挂断通话对象")
public class HangupDTO {

    @Schema(description = "通话对方的用户id")
    private String userId;
}
