package com.mingri.service.chat.repo.req.message;

import lombok.Data;

@Data
public class RetractionMsgVo {
    private String msgId;
    private String targetId;
}
