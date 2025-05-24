package com.mingri.model.vo.admin.dto;

import com.mingri.model.vo.admin.entity.Conversation;
import lombok.Data;

@Data
public class ConversationDto extends Conversation {
    private String name;
    private String portrait;
    private String account;
}
