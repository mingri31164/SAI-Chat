package com.mingri.dto.message;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class RecallDTO {
    @NotBlank(message = "消息不能为空~")
    private String msgId;
}
