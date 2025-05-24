package com.mingri.model.vo.chat.chatlist.dto;

import com.mingri.model.vo.chat.chatgroup.entity.ChatGroup;
import com.mingri.model.vo.chat.friend.dto.FriendDetailsDto;
import lombok.Data;

import java.util.List;


@Data
public class ChatDto {
    private List<FriendDetailsDto> friend;
    private List<ChatGroup> group;
}
