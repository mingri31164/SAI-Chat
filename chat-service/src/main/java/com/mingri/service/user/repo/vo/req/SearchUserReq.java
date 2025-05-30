package com.mingri.service.user.repo.vo.req;

import lombok.Data;
import javax.validation.constraints.NotNull;


@Data
public class SearchUserReq {
    @NotNull(message = "用户信息不能为空")
    private String userInfo;
}
