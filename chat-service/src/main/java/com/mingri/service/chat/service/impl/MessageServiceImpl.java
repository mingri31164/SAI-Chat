package com.mingri.service.chat.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.model.constant.MessageContentType;
import com.mingri.model.constant.MsgSource;
import com.mingri.model.constant.MsgType;
import com.mingri.model.exception.BaseException;
import com.mingri.model.vo.chat.friend.dto.FriendDetailsDto;
import com.mingri.model.vo.chat.message.dto.Top10MsgDto;
import com.mingri.model.vo.chat.chatlist.entity.ChatList;
import com.mingri.model.vo.chat.message.entity.Message;
import com.mingri.model.vo.chat.message.entity.MessageRetraction;
import com.mingri.model.vo.chat.message.dto.MsgContent;
import com.mingri.model.vo.chat.message.req.SendMsgReq;
import com.mingri.service.chat.repo.mapper.MessageMapper;
import com.mingri.model.vo.chat.message.req.expose.ThirdsendMsgReq;
import com.mingri.model.vo.chat.message.req.MessageRecordReq;
import com.mingri.model.vo.chat.message.req.ReeditMsgReq;
import com.mingri.model.vo.chat.message.req.RetractionMsgReq;
import com.mingri.service.chat.service.*;
import com.mingri.service.rocketmq.MQProducerService;
import com.mingri.model.vo.user.entity.User;
import com.mingri.service.user.service.UserService;
import com.mingri.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 消息表 服务实现类
 */
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    FriendService friendService;
    @Resource
    WebSocketService webSocketService;
    @Resource
    ChatListService chatListService;
    @Resource
    MessageMapper messageMapper;
    @Resource
    MinioUtil minioUtil;
    @Resource
    MQProducerService mqProducerService;
    @Resource
    UserService userService;
    @Resource
    MessageRetractionService  messageRetractionService;
    @Resource
    ChatGroupMemberService  chatGroupMemberService;



    private Message getMessage(String userId, MsgContent msgContent, String source, String type, String toUserId) {
        Message previousMessage = messageMapper.getPreviousShowTimeMsg(userId, toUserId);
        //存入数据库
        Message message = new Message();
        message.setId(IdUtil.randomUUID());
        message.setFromId(userId);
        message.setSource(source);
        message.setToId(toUserId);
        message.setType(type);
        if (null == previousMessage) {
            message.setIsShowTime(true);
        } else {
            message.setIsShowTime(DateUtil.between(new Date(), previousMessage.getUpdateTime(), DateUnit.MINUTE) > 5);
        }
        if (MessageContentType.Img.equals(msgContent.getType()) ||
                MessageContentType.File.equals(msgContent.getType()) ||
                MessageContentType.Voice.equals(msgContent.getType())) {
            JSONObject content = JSONUtil.parseObj(msgContent.getContent());
            String name = (String) content.get("name");
            String fileType = name.substring(name.lastIndexOf(".") + 1);
            String fileName = userId + "/" + toUserId + "/" + IdUtil.randomUUID() + "." + fileType;
            content.set("fileName", fileName);
            content.set("url", minioUtil.getUrl(fileName));
            content.set("type", fileType);
            msgContent.setContent(content.toJSONString(0));
        }
        message.setMsgContent(msgContent);
        return message;
    }

    /**
     * 当没有sendMsgReq时，使用此方法发送消息
     */
    public Message sendMessage(String userId, String toUserId, MsgContent msgContent, String source, String type) {
        Message message = getMessage(userId, msgContent, source, type, toUserId);
        boolean isSave = save(message);
        if (isSave) return message;
        return null;
    }

    /**
     * 发送消息，通过sendMsgReq判断是否为转发消息
     */
    public Message sendMessage(String userId, SendMsgReq sendMsgReq, MsgContent msgContent , String source, String type) {
        final String toUserId = sendMsgReq.getToUserId();
        //获取上一条显示时间的消息
        Message message = getMessage(userId, msgContent, source, type, toUserId);
        if (null != sendMsgReq.getIsForward() && sendMsgReq.getIsForward())
            message.setFromForwardMsgId(sendMsgReq.getFromMsgId());
        boolean isSave = save(message);
        if (isSave) return message;
        return null;
    }

    public Message sendMessageToUser(String userId, SendMsgReq sendMsgReq, String type) {
        //验证是否是好友
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, sendMsgReq.getToUserId());
        if (!isFriend) throw new BaseException("双方非好友");
        Message message = sendMessage(userId, sendMsgReq,sendMsgReq.getMsgContent(), MsgSource.User, type);
        MsgContent msgContent = message.getMsgContent();

        FriendDetailsDto friendDetails = friendService.getFriendDetails(sendMsgReq.getToUserId(), userId);
        msgContent.setFormUserId(userId);
        msgContent.setFormUserName(StringUtils.isNotBlank(friendDetails.getRemark())
                ? friendDetails.getRemark() : friendDetails.getName());
        msgContent.setFormUserPortrait(friendDetails.getPortrait());

        chatListService.updateChatList(message.getToId(), userId, msgContent, MsgSource.User);
        try {
            mqProducerService.sendMsgToUser(message);
        } catch (Exception e) {
            //发送消息
            webSocketService.sendMsgToUser(message, message.getToId());
        }
        return message;

    }

    public Message sendMessageToGroup(String userId, SendMsgReq sendMsgReq, String type) {
        //获取发送方用户信息
        User user = userService.getById(userId);
        MsgContent msgContent = sendMsgReq.getMsgContent();
        msgContent.setFormUserName(user.getName());
        msgContent.setFormUserPortrait(user.getPortrait());
        Message message = sendMessage(userId, sendMsgReq, msgContent, MsgSource.Group, type);

        chatListService.updateChatListGroup(message.getToId(), message.getMsgContent());
        try {
            mqProducerService.sendMsgToGroup(message);
        } catch (Exception e) {
            //发送消息
            webSocketService.sendMsgToGroup(message, message.getToId());
        }
        return message;
    }

    @Override
    public Message sendMessage(String userId, String role, SendMsgReq sendMsgReq, String type) {
        if (MsgSource.Group.equals(sendMsgReq.getSource())) {
            return sendMessageToGroup(userId, sendMsgReq, type);
        } else {
            return sendMessageToUser(userId, sendMsgReq, type);
        }
    }

    @Override
    public List<Message> messageRecord(String userId, MessageRecordReq messageRecordReq) {
        List<Message> messages = messageMapper.messageRecord(userId, messageRecordReq.getTargetId(),
                messageRecordReq.getIndex(), messageRecordReq.getNum());
        return messages;
    }

    @Override
    public List<Message> messageRecordDesc(String userId, MessageRecordReq messageRecordReq) {
        List<Message> messages = messageMapper.messageRecordDesc(userId, messageRecordReq.getTargetId(),
                messageRecordReq.getIndex(), messageRecordReq.getNum());
        return messages;
    }

    @Override
    public Message sendFileMessageToUser(String userId, String toUserId, JSONObject fileInfo) {
        MsgContent msgContent = new MsgContent();
        msgContent.setContent(fileInfo.toJSONString(0));
        msgContent.setType(MessageContentType.File);
        return sendMessage(userId, toUserId, msgContent, MsgSource.User, MsgType.User);
    }

    @Override
    public MsgContent getFileMsgContent(String userId, String msgId) {
        Message msg = getById(msgId);
        if (msg == null) {
            throw new BaseException("消息为空");
        }
        if (msg.getFromId().equals(userId) || msg.getToId().equals(userId)
                || chatGroupMemberService.isMemberExists(msg.getToId(), userId)) {
            return msg.getMsgContent();
        } else {
            throw new BaseException("消息为空");
        }
    }

    @Override
    public boolean updateMsgContent(String msgId, MsgContent msgContent) {
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Message::getMsgContent, msgContent)
                .eq(Message::getId, msgId);
        return update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Message retractionMsg(String userId, RetractionMsgReq retractionMsgReq) {

        Message message = getById(retractionMsgReq.getMsgId());
        if (null == message)
            throw new BaseException("消息不存在");
        MsgContent msgContent = message.getMsgContent();
        if (MsgSource.User.equals(message.getSource())) {
            FriendDetailsDto friendDetails = friendService.getFriendDetails(message.getToId(), userId);
            msgContent.setFormUserName(StringUtils.isNotBlank(friendDetails.getRemark())
                    ? friendDetails.getRemark() : friendDetails.getName());
        }

        msgContent.setExt(msgContent.getType());
        //只有文本才保存，之前的消息内容
        if (MessageContentType.Text.equals(msgContent.getType())) {
            MessageRetraction messageRetraction = new MessageRetraction();
            messageRetraction.setMsgId(IdUtil.randomUUID());
            messageRetraction.setMsgId(message.getId());
            messageRetraction.setMsgContent(msgContent);
            messageRetractionService.save(messageRetraction);
        }
        msgContent.setType(MessageContentType.Retraction);
        msgContent.setContent("");
        updateById(message);

        ChatList userIdchatList = chatListService.getChatListByUserIdAndFromId(userId, message.getToId());
        userIdchatList.setLastMsgContent(msgContent);
        chatListService.updateById(userIdchatList);
        ChatList toIdchatList;
        if (MsgSource.User.equals(message.getSource()))
            toIdchatList = chatListService.getChatListByUserIdAndFromId(message.getToId(), userId);
        else
            toIdchatList = chatListService.getChatListByUserIdAndFromId(message.getFromId(), message.getToId());

        toIdchatList.setLastMsgContent(msgContent);
        chatListService.updateById(toIdchatList);
        if (message.getSource().equals(MsgSource.User))
            webSocketService.sendMsgToUser(message, message.getToId());
        if (message.getSource().equals(MsgSource.Group))
            webSocketService.sendMsgToGroup(message, retractionMsgReq.getTargetId());

        return message;
    }

    @Override
    public MessageRetraction reeditMsg(String userId, ReeditMsgReq reeditMsgReq) {
        LambdaQueryWrapper<MessageRetraction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageRetraction::getMsgId, reeditMsgReq.getMsgId());
        return messageRetractionService.getOne(queryWrapper);
    }

    @Override
    public String sendFileOrImg(String userId, String msgId, InputStream inputStream) throws IOException {
        MsgContent msgContent = getFileMsgContent(userId, msgId);
        JSONObject fileInfo = JSONUtil.parseObj(msgContent.getContent());
        String url = minioUtil.uploadFile(inputStream, fileInfo.get("fileName").toString(), fileInfo.getLong("size"));
        return url;
    }

    @Override
    public Message voiceToText(String userId, String msgId) {
        Message message = getById(msgId);
        if (null == message || !MessageContentType.Voice.equals(message.getMsgContent().getType())) {
            throw new BaseException("这不是一条语音~");
        }
        if (!message.getToId().equals(userId) && !message.getFromId().equals(userId)) {
            throw new BaseException("不能查看其他~");
        }
        return getVoiceMessage(message);
    }

    @Override
    public Message voiceToText(String userId, String msgId,Boolean isChatGroupMessage) {
        Message message = getById(msgId);
        if (null == message || !MessageContentType.Voice.equals(message.getMsgContent().getType())) {
            throw new BaseException("这不是一条语音~");
        }
        if (!message.getToId().equals(userId) && !message.getFromId().equals(userId)&&!isChatGroupMessage) {
            throw new BaseException("不能查看其他~");
        }
        return getVoiceMessage(message);
    }

    private Message getVoiceMessage(Message message) {
        // TODO 语音转换
        return null;
    }


    @Override
    public Integer messageNum(DateTime date) {
        Integer num = messageMapper.messageNum(date);
        return num;
    }

    @Override
    public List<Top10MsgDto> getTop10Msg(Date date) {
        List<Top10MsgDto> result = messageMapper.getTop10Msg(date);
        return result;
    }

    @Override
    public boolean thirdPartySendMsg(String userId, ThirdsendMsgReq thirdsendMsgReq) {
        if (userId == null)
            return false;
        List<User> users = userService.getUserByEmail(thirdsendMsgReq.getEmail());
        for (User user : users) {
            MsgContent msgContent = new MsgContent();
            msgContent.setType(MessageContentType.Text);
            msgContent.setContent(thirdsendMsgReq.getContent());

            SendMsgReq sendMsgReq = new SendMsgReq();
            sendMsgReq.setMsgContent(msgContent);
            sendMsgReq.setToUserId(user.getId());
            sendMsgReq.setSource(MsgSource.User);
            sendMessageToUser(userId, sendMsgReq, MsgType.User);
        }
        return true;
    }


}
