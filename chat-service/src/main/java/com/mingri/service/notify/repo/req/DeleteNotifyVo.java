package com.mingri.service.notify.repo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteNotifyVo {
    @NotNull(message = "通知id不能为空")
    private String notifyId;
}
