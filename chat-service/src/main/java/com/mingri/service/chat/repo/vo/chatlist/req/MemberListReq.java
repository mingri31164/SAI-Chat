package com.mingri.service.chat.repo.vo.chatlist.req;


import lombok.Data;

@Data
public class MemberListReq {
    public String chatGroupId;
    //起始
    private int index = 0;
    //查询条数
    private int num = 10;
}
