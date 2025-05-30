package com.mingri.service.chat.repo.vo.message.dto;

import com.mingri.service.user.repo.vo.entity.User;
import lombok.Data;

@Data
public class Top10MsgDto extends User {
    private int num;
}
