package com.mingri.service.admin.repo.vo.req.user;

import lombok.Data;

@Data
public class UserListReq {
    //起始
    private int currentPage = 1;
    //查询条数
    private int pageSize = 10;
    //查询关键字
    private String keyword;
    //在线状态
    private String onlineStatus;
}
