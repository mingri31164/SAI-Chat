package com.mingri.model.vo.talk.req;

import lombok.Data;

import java.util.List;

@Data
public class CreateTalkReq {
    private String text;
    private List<String> permission;
}
