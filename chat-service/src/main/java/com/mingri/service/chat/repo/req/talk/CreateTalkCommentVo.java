package com.mingri.service.chat.repo.req.talk;

import lombok.Data;

@Data
public class CreateTalkCommentVo {
    private String talkId;
    private String comment;
}
