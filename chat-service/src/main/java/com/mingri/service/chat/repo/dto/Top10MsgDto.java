package com.mingri.service.chat.repo.dto;

import com.mingri.service.user.repo.entity.User;
import lombok.Data;

@Data
public class Top10MsgDto extends User {
    private int num;
}
