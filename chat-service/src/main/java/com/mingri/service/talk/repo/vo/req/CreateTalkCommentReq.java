package com.mingri.service.talk.repo.vo.req;

import lombok.Data;

@Data
public class CreateTalkCommentReq {
    private String talkId;
    private String comment;
}
