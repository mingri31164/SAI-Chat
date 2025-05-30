package com.mingri.service.talk.repo.vo.req;

import lombok.Data;

@Data
public class TalkListReq {
    //起始
    private int index;
    //查询条数
    private int num;
    private String targetId;
}
