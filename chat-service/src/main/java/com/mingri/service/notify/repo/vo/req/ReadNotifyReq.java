package com.mingri.service.notify.repo.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReadNotifyReq {

    @NotNull(message = "通知类型")
    private String notifyType;
}
