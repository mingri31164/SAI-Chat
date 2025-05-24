package com.mingri.model.vo.user.req;

import lombok.Data;

/**
 * 用户设置表更新对象
 **/
@Data
public class UpdateUserSetReq {
    private String key;
    private String value;
}
