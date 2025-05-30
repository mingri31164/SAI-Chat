package com.mingri.service.notify.repo.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteNotifyReq {
    @NotNull(message = "通知id不能为空")
    private String notifyId;
}
