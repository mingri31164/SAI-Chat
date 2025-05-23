package com.mingri.service.chat.repo.dto;

import com.mingri.service.notify.repo.entity.Notify;
import lombok.Data;

@Data
public class FriendNotifyDto extends Notify {
    private String fromName;
    private String fromPortrait;
    private String toName;
    private String toPortrait;
}
