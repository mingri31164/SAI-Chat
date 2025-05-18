package com.mingri.service.chat.repo.req.chatlist;


import lombok.Data;

@Data
public class MemberListVo {
    public String chatGroupId;
    //起始
    private int index = 0;
    //查询条数
    private int num = 10;
}
