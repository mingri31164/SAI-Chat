package com.mingri.model.vo.talk.req;

import lombok.Data;

@Data
public class CreateTalkCommentReq {
    private String talkId;
    private String comment;
}
