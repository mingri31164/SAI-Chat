package com.mingri.service;

import com.mingri.dto.message.RecallDTO;
import com.mingri.dto.message.RecordDTO;
import com.mingri.dto.message.SendMessageDTO;
import com.mingri.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.vo.chatGroup.SendMsgVo;

import java.time.LocalDate;
import java.util.List;


public interface IMessageService extends IService<Message> {

    Message send(SendMessageDTO sendMessageDTO);
    Message sendMessage(String userId, String role, SendMsgVo sendMsgVo, String type);

    List<Message> record(RecordDTO recordDTO);

    Message recall(RecallDTO recallDTO);

    void deleteExpiredMessages(LocalDate expirationDate);

    Message sendMessageToGroup(String userId, SendMessageDTO sendMessageDTO);
}
