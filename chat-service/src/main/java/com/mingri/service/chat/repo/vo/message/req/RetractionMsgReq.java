package com.mingri.service.chat.repo.vo.message.req;

import lombok.Data;

@Data
public class RetractionMsgReq {
    private String msgId;
    private String targetId;
}
