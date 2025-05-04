package com.mingri.dto.call;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "邀请通话对象")
public class InviteDTO {

    @Schema(description = "邀请目标的用户id")
    private String userId;

    @Schema(description = "是否仅音频")
    private boolean isOnlyAudio;

}
