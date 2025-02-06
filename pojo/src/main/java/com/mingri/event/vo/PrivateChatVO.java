package com.mingri.event.vo;

import com.mingri.dto.message.SendMessageDTO;
import com.mingri.entity.Message;
import lombok.Data;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/2/6 22:45
 * @ClassName: PrivateChatVO
 * @Version: 1.0
 */

@Data
public class PrivateChatVO {

    private SendMessageDTO sendMessageDTO;
    private Message message;

    public PrivateChatVO(SendMessageDTO sendMessageDTO, Message message) {
        this.sendMessageDTO = sendMessageDTO;
        this.message = message;
    }

}
