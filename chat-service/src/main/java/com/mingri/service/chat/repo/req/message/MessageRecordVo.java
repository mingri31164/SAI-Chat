package com.mingri.service.chat.repo.req.message;

import lombok.Data;

@Data
public class MessageRecordVo {
    //目标id
    private String targetId;
    //起始
    private int index;
    //查询条数
    private int num;
}
