package com.mingri.service.chat.repo.req.chatGroupNotice;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateNoticeVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "公告内容不能为空~")
    private String content;
}
