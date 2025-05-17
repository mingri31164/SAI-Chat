package com.mingri.service.chat.repo.dto;

import com.mingri.service.chat.repo.entity.ChatGroup;
import lombok.Data;

import java.util.List;


@Data
public class ChatDto {
    private List<FriendDetailsDto> friend;
    private List<ChatGroup> group;
}
