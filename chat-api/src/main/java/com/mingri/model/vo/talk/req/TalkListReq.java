package com.mingri.model.vo.talk.req;

import lombok.Data;

@Data
public class TalkListReq {
    //起始
    private int index;
    //查询条数
    private int num;
    private String targetId;
}
