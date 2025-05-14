package com.mingri.service;

import com.mingri.pojo.dto.message.RecallDTO;
import com.mingri.pojo.dto.message.RecordDTO;
import com.mingri.pojo.dto.message.SendMessageDTO;
import com.mingri.pojo.entity.chat.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.pojo.vo.chatGroup.SendMsgVo;

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
