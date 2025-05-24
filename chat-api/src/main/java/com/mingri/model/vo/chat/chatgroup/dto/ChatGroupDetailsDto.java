package com.mingri.model.vo.chat.chatgroup.dto;

import com.mingri.model.vo.chat.chatgroup.entity.ChatGroupNotice;
import lombok.Data;

@Data
public class ChatGroupDetailsDto {
    private String id;
    private String chatGroupNumber;
    private String userId;
    private String ownerUserId;
    private String portrait;
    private String name;
    private ChatGroupNotice notice;
    private String memberNum;
    private String groupName;
    private String groupRemark;
    private String createTime;
    private String updateTime;
}
