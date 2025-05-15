package com.mingri.model.vo.chat.chatlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除私聊对象")
public class DeleteDTO {

    @Schema(description = "私聊对象的ID")
    private String chatListId;
}
