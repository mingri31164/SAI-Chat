package com.mingri.service.chat.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.Top10MsgDto;
import com.mingri.service.chat.repo.entity.Message;
import com.mingri.service.chat.repo.entity.MessageRetraction;
import com.mingri.service.chat.repo.entity.ext.MsgContent;
import com.mingri.service.chat.repo.req.SendMsgVo;
import com.mingri.service.chat.repo.req.expose.ThirdSendMsgVo;
import com.mingri.service.chat.repo.req.message.MessageRecordVo;
import com.mingri.service.chat.repo.req.message.ReeditMsgVo;
import com.mingri.service.chat.repo.req.message.RetractionMsgVo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 消息表 服务类
 */
public interface MessageService extends IService<Message> {

    Message sendMessage(String userId, String role, SendMsgVo sendMsgVo, String type);

    List<Message> messageRecord(String userId, MessageRecordVo messageRecordVo);

    List<Message> messageRecordDesc(String userId, MessageRecordVo messageRecordVo);

    Message sendFileMessageToUser(String userId, String toUserId, JSONObject fileInfo);

    MsgContent getFileMsgContent(String userId, String msgId);

    boolean updateMsgContent(String msgId, MsgContent msgContent);

    Message retractionMsg(String userId, RetractionMsgVo retractionMsgVo);

    MessageRetraction reeditMsg(String userId, ReeditMsgVo reeditMsgVo);

    String sendFileOrImg(String userId, String msgId, InputStream request) throws IOException;

    Message voiceToText(String userId, String msgId);
    Message voiceToText(String userId, String msgId,Boolean isChatGroupMessage);

    Integer messageNum(DateTime date);

    List<Top10MsgDto> getTop10Msg(Date date);

    boolean thirdPartySendMsg(String userId, ThirdSendMsgVo sendMsgVo);

}
