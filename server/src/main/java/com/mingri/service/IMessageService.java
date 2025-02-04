package com.mingri.service;

import com.mingri.dto.message.RecallDTO;
import com.mingri.dto.message.RecordDTO;
import com.mingri.dto.message.SendMessageDTO;
import com.mingri.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDate;
import java.util.List;


public interface IMessageService extends IService<Message> {

    Message send(SendMessageDTO sendMessageDTO);

    List<Message> record(RecordDTO recordDTO);

    Message recall(RecallDTO recallDTO);

    void deleteExpiredMessages(LocalDate expirationDate);

    Message sendMessageToGroup(String userId, SendMessageDTO sendMessageDTO);
}
