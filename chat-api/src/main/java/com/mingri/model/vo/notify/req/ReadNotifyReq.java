package com.mingri.model.vo.notify.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReadNotifyReq {

    @NotNull(message = "通知类型")
    private String notifyType;
}
