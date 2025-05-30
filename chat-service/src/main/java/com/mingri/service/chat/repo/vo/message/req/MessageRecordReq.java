package com.mingri.service.chat.repo.vo.message.req;

import lombok.Data;

@Data
public class MessageRecordReq {
    //目标id
    private String targetId;
    //起始
    private int index;
    //查询条数
    private int num;
}
