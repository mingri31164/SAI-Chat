package com.mingri.service.chat.repo.req.talk;

import lombok.Data;

@Data
public class TalkListVo {
    //起始
    private int index;
    //查询条数
    private int num;
    private String targetId;
}
