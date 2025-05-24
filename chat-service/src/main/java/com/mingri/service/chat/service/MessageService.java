package com.mingri.service.chat.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.chat.message.dto.Top10MsgDto;
import com.mingri.model.vo.chat.message.entity.Message;
import com.mingri.model.vo.chat.message.entity.MessageRetraction;
import com.mingri.model.vo.chat.message.dto.MsgContent;
import com.mingri.model.vo.chat.message.req.SendMsgReq;
import com.mingri.model.vo.chat.message.req.expose.ThirdsendMsgReq;
import com.mingri.model.vo.chat.message.req.MessageRecordReq;
import com.mingri.model.vo.chat.message.req.ReeditMsgReq;
import com.mingri.model.vo.chat.message.req.RetractionMsgReq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 消息表 服务类
 */
public interface MessageService extends IService<Message> {

    Message sendMessage(String userId, String role, SendMsgReq sendMsgReq, String type);

    List<Message> messageRecord(String userId, MessageRecordReq messageRecordReq);

    List<Message> messageRecordDesc(String userId, MessageRecordReq messageRecordReq);

    Message sendFileMessageToUser(String userId, String toUserId, JSONObject fileInfo);

    MsgContent getFileMsgContent(String userId, String msgId);

    boolean updateMsgContent(String msgId, MsgContent msgContent);

    Message retractionMsg(String userId, RetractionMsgReq retractionMsgReq);

    MessageRetraction reeditMsg(String userId, ReeditMsgReq reeditMsgReq);

    String sendFileOrImg(String userId, String msgId, InputStream request) throws IOException;

    Message voiceToText(String userId, String msgId);
    Message voiceToText(String userId, String msgId,Boolean isChatGroupMessage);

    Integer messageNum(DateTime date);

    List<Top10MsgDto> getTop10Msg(Date date);

    boolean thirdPartySendMsg(String userId, ThirdsendMsgReq sendMsgReq);

}
