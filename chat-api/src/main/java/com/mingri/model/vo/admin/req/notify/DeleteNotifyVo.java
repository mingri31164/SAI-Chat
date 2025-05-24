package com.mingri.model.vo.admin.req.notify;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteNotifyVo {
    @NotNull(message = "通知不能为空")
    private String notifyId;
}
