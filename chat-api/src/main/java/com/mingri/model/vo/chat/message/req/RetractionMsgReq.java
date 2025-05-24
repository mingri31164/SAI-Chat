package com.mingri.model.vo.chat.message.req;

import lombok.Data;

@Data
public class RetractionMsgReq {
    private String msgId;
    private String targetId;
}
