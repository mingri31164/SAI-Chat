package com.mingri.model.vo.chat.message.dto;

import com.mingri.model.vo.user.entity.User;
import lombok.Data;

@Data
public class Top10MsgDto extends User {
    private int num;
}
