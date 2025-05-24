package com.mingri.model.vo.chat.friend.dto;

import com.mingri.model.vo.notify.entity.Notify;
import lombok.Data;

@Data
public class FriendNotifyDto extends Notify {
    private String fromName;
    private String fromPortrait;
    private String toName;
    private String toPortrait;
}
