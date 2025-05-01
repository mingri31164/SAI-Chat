package com.mingri.event.vo;

import com.mingri.entity.Message;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/2/6 22:45
 * @ClassName: PrivateChatVO
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
public class PrivateChatVO {

    private String userId;
    private String targetId;
    private Message message;

    public PrivateChatVO(String userId, String targetId, Message message) {
        this.userId = userId;
        this.targetId = targetId;
        this.message = message;
    }

}
