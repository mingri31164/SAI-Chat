package com.mingri.model.vo.talk.req;

import lombok.Data;

@Data
public class DeleteTalkCommentReq {
    private String talkId;
    private String talkCommentId;
}
