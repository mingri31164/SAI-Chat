package com.mingri.service.admin.repo.vo.dto;

import com.mingri.service.admin.repo.vo.entity.Conversation;
import lombok.Data;

@Data
public class ConversationDto extends Conversation {
    private String name;
    private String portrait;
    private String account;
}
