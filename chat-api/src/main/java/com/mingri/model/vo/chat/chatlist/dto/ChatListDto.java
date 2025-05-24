package com.mingri.model.vo.chat.chatlist.dto;

import com.mingri.model.vo.chat.chatlist.entity.ChatList;
import lombok.Data;

import java.util.List;

@Data
public class ChatListDto {
    private List<ChatList> tops;
    private List<ChatList> others;
}
