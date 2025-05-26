package com.mingri.model.vo.admin.req.notify;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteNotifyReq {
    @NotNull(message = "通知不能为空")
    private String notifyId;
}
