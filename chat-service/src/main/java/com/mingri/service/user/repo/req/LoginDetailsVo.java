package com.mingri.service.user.repo.req;

import lombok.Data;

@Data
public class LoginDetailsVo {
    //起始
    private int index;
    //查询条数
    private int num;
    //查询关键字
    private String keyword;
}
