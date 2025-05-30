package com.mingri.service.chat.repo.vo.chatlist.dto;

import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroup;
import com.mingri.service.chat.repo.vo.friend.dto.FriendDetailsDto;
import lombok.Data;

import java.util.List;


@Data
public class ChatDto {
    private List<FriendDetailsDto> friend;
    private List<ChatGroup> group;
}
