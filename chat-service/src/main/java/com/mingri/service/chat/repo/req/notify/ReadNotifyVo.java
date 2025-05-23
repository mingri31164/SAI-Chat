package com.mingri.service.chat.repo.req.notify;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReadNotifyVo {

    @NotNull(message = "通知类型")
    private String notifyType;
}
