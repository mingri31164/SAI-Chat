package com.mingri.model.vo.notify.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteNotifyReq {
    @NotNull(message = "通知id不能为空")
    private String notifyId;
}
