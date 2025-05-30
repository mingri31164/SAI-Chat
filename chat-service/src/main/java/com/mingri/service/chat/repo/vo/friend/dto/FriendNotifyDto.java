package com.mingri.service.chat.repo.vo.friend.dto;

import com.mingri.service.notify.repo.vo.entity.Notify;
import lombok.Data;

@Data
public class FriendNotifyDto extends Notify {
    private String fromName;
    private String fromPortrait;
    private String toName;
    private String toPortrait;
}
