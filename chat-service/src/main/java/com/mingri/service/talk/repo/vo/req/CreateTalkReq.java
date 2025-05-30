package com.mingri.service.talk.repo.vo.req;

import lombok.Data;

import java.util.List;

@Data
public class CreateTalkReq {
    private String text;
    private List<String> permission;
}
