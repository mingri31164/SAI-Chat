package com.mingri.service.chat.repo.vo.message.req.expose;

import lombok.Data;

@Data
public class ThirdsendMsgReq {
    private String email;
    private String content;
}
