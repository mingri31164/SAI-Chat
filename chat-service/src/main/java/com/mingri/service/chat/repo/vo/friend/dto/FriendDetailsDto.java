package com.mingri.service.chat.repo.vo.friend.dto;

import com.mingri.service.talk.repo.vo.dto.TalkContentDto;
import lombok.Data;

import java.util.Date;

@Data
public class FriendDetailsDto {
    private String friendId;
    private String account;
    private String name;
    private String sex;
    private Date birthday;
    private String signature;
    private String remark;
    private String groupName;
    private String portrait;
    private boolean isConcern;
    private TalkContentDto talkContent;
}
