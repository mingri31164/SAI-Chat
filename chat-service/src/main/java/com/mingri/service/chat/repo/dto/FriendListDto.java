package com.mingri.service.chat.repo.dto;

import com.mingri.service.chat.repo.entity.Friend;
import lombok.Data;

import java.util.List;

@Data
public class FriendListDto {
    //分组id
    private String groupId;
    //是否自定义分组
    private boolean isCustom;
    //分组名称
    private String name;
    //分组对应好友
    private List<Friend> friends;
}
