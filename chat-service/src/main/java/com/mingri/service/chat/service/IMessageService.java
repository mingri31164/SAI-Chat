package com.mingri.service.chat.service;

import com.mingri.model.vo.chat.message.dto.RecallDTO;
import com.mingri.model.vo.chat.message.dto.RecordDTO;
import com.mingri.model.vo.chat.message.dto.SendMessageDTO;
import com.mingri.service.chat.repo.entity.MessageDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;


public interface IMessageService extends IService<MessageDO> {

    MessageDO send(SendMessageDTO sendMessageDTO);

    List<MessageDO> record(RecordDTO recordDTO);

    MessageDO recall(RecallDTO recallDTO);

    void deleteExpiredMessages(LocalDate expirationDate);

    MessageDO sendMessageToGroup(String userId, SendMessageDTO sendMessageDTO);
}
