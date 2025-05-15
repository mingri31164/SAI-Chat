package com.mingri.model.vo.chat.message.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
public class SystemMsgDto {
    @AllArgsConstructor
    @Data
    class Content {
        private boolean isEmphasize;
//        @SensitiveField(bind = "content")
        private String content;
    }

    private ArrayList<Content> contents;

    public SystemMsgDto() {
        contents = new ArrayList<>();
    }

    public SystemMsgDto addEmphasizeContent(String content) {
        contents.add(new Content(true, content));
        return this;
    }

    public SystemMsgDto addContent(String content) {
        contents.add(new Content(false, content));
        return this;
    }
}
