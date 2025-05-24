package com.mingri.model.vo.user.req;

import lombok.Data;

@Data
public class LoginDetailsReq {
    //起始
    private int index;
    //查询条数
    private int num;
    //查询关键字
    private String keyword;
}
