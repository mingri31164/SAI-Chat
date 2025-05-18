package com.mingri.service.chat.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.model.constant.MessageContentType;
import com.mingri.model.constant.MsgSource;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.repo.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.entity.Message;
import com.mingri.service.chat.repo.entity.ext.MsgContent;
import com.mingri.service.chat.repo.mapper.MessageMapper;
import com.mingri.service.chat.repo.req.SendMsgVo;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.chat.service.FriendService;
import com.mingri.service.chat.service.MessageService;
import com.mingri.service.rocketmq.MQProducerService;
import com.mingri.service.user.repo.entity.User;
import com.mingri.service.user.service.UserService;
import com.mingri.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

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
     * 当没有sendMsgVo时，使用此方法发送消息
     */
    public Message sendMessage(String userId, String toUserId, MsgContent msgContent, String source, String type) {
        Message message = getMessage(userId, msgContent, source, type, toUserId);
        boolean isSave = save(message);
        if (isSave) return message;
        return null;
    }

    /**
     * 发送消息，通过sendMsgVo判断是否为转发消息
     */
    public Message sendMessage(String userId, SendMsgVo sendMsgVo, MsgContent msgContent , String source, String type) {
        final String toUserId = sendMsgVo.getToUserId();
        //获取上一条显示时间的消息
        Message message = getMessage(userId, msgContent, source, type, toUserId);
        if (null != sendMsgVo.getIsForward() && sendMsgVo.getIsForward())
            message.setFromForwardMsgId(sendMsgVo.getFromMsgId());
        boolean isSave = save(message);
        if (isSave) return message;
        return null;
    }

    public Message sendMessageToUser(String userId, SendMsgVo sendMsgVo, String type) {
        //验证是否是好友
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, sendMsgVo.getToUserId());
        if (!isFriend) throw new BaseException("双方非好友");
        Message message = sendMessage(userId, sendMsgVo,sendMsgVo.getMsgContent(), MsgSource.User, type);
        MsgContent msgContent = message.getMsgContent();
        // TODO 获取好友信息
        FriendDetailsDto friendDetails = friendService.getFriendDetails(sendMsgVo.getToUserId(), userId);
        msgContent.setFormUserId(userId);
        msgContent.setFormUserName(StringUtils.isNotBlank(friendDetails.getRemark())
                ? friendDetails.getRemark() : friendDetails.getName());
        msgContent.setFormUserPortrait(friendDetails.getPortrait());
        // TODO 更新聊天列表
//        chatListService.updateChatList(message.getToId(), userId, msgContent, MsgSource.User);
        try {
            mqProducerService.sendMsgToUser(message);
        } catch (Exception e) {
            //发送消息
            webSocketService.sendMsgToUser(message, message.getToId());
        }
        return message;

    }

    public Message sendMessageToGroup(String userId, SendMsgVo sendMsgVo, String type) {
        //获取发送方用户信息
        User user = userService.getById(userId);
        MsgContent msgContent = sendMsgVo.getMsgContent();
        msgContent.setFormUserName(user.getName());
        msgContent.setFormUserPortrait(user.getPortrait());
        Message message = sendMessage(userId, sendMsgVo, msgContent, MsgSource.Group, type);
        // TODO 更新聊天列表
//        chatListService.updateChatListGroup(message.getToId(), message.getMsgContent());
        try {
            mqProducerService.sendMsgToGroup(message);
        } catch (Exception e) {
            //发送消息
            webSocketService.sendMsgToGroup(message, message.getToId());
        }
        return message;
    }

    @Override
    public Message sendMessage(String userId, String role, SendMsgVo sendMsgVo, String type) {
        if (MsgSource.Group.equals(sendMsgVo.getSource())) {
            return sendMessageToGroup(userId, sendMsgVo, type);
        } else {
            return sendMessageToUser(userId, sendMsgVo, type);
        }
    }


}
