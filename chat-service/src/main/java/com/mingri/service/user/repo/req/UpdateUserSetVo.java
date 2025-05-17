package com.mingri.service.user.repo.req;

import lombok.Data;

/**
 * 用户设置表更新对象
 **/
@Data
public class UpdateUserSetVo {
    private String key;
    private String value;
}
