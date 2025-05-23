package com.mingri.service.chat.repo.req.talk;

import lombok.Data;

@Data
public class DeleteTalkCommentVo {
    private String talkId;
    private String talkCommentId;
}
