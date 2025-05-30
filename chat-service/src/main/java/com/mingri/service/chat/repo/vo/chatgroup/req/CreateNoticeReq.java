package com.mingri.service.chat.repo.vo.chatgroup.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateNoticeReq {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "公告内容不能为空~")
    private String content;
}
