package com.mingri.service.chat.repo.vo.chatlist.dto;

import com.mingri.service.chat.repo.vo.chatlist.entity.ChatList;
import lombok.Data;

import java.util.List;

@Data
public class ChatListDto {
    private List<ChatList> tops;
    private List<ChatList> others;
}
