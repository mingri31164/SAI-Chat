package com.mingri.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mingri.constant.*;
import com.mingri.constant.type.MessageType;
import com.mingri.constant.type.TextContentType;
import com.mingri.context.BaseContext;
import com.mingri.dto.message.RecallDTO;
import com.mingri.dto.message.RecordDTO;
import com.mingri.dto.message.SendMessageDTO;
import com.mingri.dto.message.TextMessageContent;
import com.mingri.entity.Message;
import com.mingri.enumeration.NotifyTypeEnum;
import com.mingri.enumeration.UserTypes;
import com.mingri.event.NotifyMsgEvent;
import com.mingri.exception.BaseException;
import com.mingri.mapper.MessageMapper;
import com.mingri.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.utils.CacheUtil;
import com.mingri.vo.SysUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;



@Slf4j
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    private IChatListService chatListService;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private CacheUtil cacheUtil;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private SensitiveWordBs sensitiveWordBs;
    @Autowired
    private AiChatService aiChatService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Message send(SendMessageDTO sendMessageDTO) {
        if (MessageSource.Group.equals(sendMessageDTO.getSource())) {
            return sendMessageToGroup(BaseContext.getCurrentId().toString(),sendMessageDTO);
        } else {
            return sendMessageToUser(sendMessageDTO);
        }
    }

    @Override
    //@DS("slave")
    public List<Message> record(RecordDTO recordDTO) {
        String userId = BaseContext.getCurrentId();
        List<Message> messages = messageMapper.record(userId, recordDTO.getTargetId(),
                recordDTO.getIndex(), recordDTO.getNum());
        cacheUtil.putUserReadCache(userId, recordDTO.getTargetId());
        return messages;
    }

    @Override
    public Message recall(RecallDTO recallDTO) {
        String userId = BaseContext.getCurrentId();
        Message message = getById(recallDTO.getMsgId());
        if (null == message) {
            throw new BaseException(MessageConstant.MESSAGE_NOT_EXIST);
        }
        if (!message.getFromId().equals(userId)) {
            throw new BaseException(MessageConstant.ERROR_ONLY_SELF_RECALL);
        }

        if (DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE) > 2) {
            throw new BaseException(MessageConstant.ERROR_EXPIRE_RECALL);
        }

        //撤回自己的消息
        message.setType(MessageType.Recall);
        message.setMessage("");
        updateById(message);
        if (MessageSource.Group.equals(message.getSource())) {
            chatListService.updateChatListGroup(message);
            webSocketService.sendMsgToGroup(message);
        } else {
            chatListService.updateChatListPrivate(message.getToId(), message);
            webSocketService.sendMsgToUser(message, userId, message.getToId());
        }
        return message;    }

    @Override
    public void deleteExpiredMessages(LocalDate expirationDate) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(Message::getCreateTime, expirationDate);
        if (remove(queryWrapper)) {
            log.info("---清理过期消息成功---");
        }
    }

    @Override
    public Message sendMessageToGroup(String userId, SendMessageDTO sendMessageDTO) {
        Message message = sendMessage(userId, sendMessageDTO , MessageSource.Group);
        NotifyMsgEvent<Message> sendMessageToGroupEvent =
                new NotifyMsgEvent<>(this, NotifyTypeEnum.GROUP_CHAT, message);
        eventPublisher.publishEvent(sendMessageToGroupEvent);
        return message;
    }

    private Message sendMessageToUser(SendMessageDTO sendMessageDTO) {
        String userId = BaseContext.getCurrentId();
        String targetId = sendMessageDTO.getTargetId();
        Message message = sendMessage(userId, sendMessageDTO, MessageSource.User);
        chatListService.updateChatListPrivate(targetId, message);
        webSocketService.sendMsgToUser(message, userId, targetId);
        return message;
    }


    public Message sendMessage(String userId, SendMessageDTO sendMessageDTO, String source) {
        //获取上一条显示时间的消息
        Message previousMessage = messageMapper.getPreviousShowTimeMsg(userId, sendMessageDTO.getTargetId());
        //存入数据库
        Message message = new Message();
        message.setId(IdUtil.randomUUID());
        message.setFromId(userId);
        message.setSource(source);
        message.setToId(sendMessageDTO.getTargetId());
        StringBuffer sb = new StringBuffer();
        AtomicReference<SysUserInfoVO> botUserRef = new AtomicReference<>(null);
        if (MessageType.Text.equals(sendMessageDTO.getType())) {
            // 敏感词替换
            List<TextMessageContent> contents = JSONUtil.toList(sendMessageDTO.getMsgContent(), TextMessageContent.class);
            contents.forEach(content -> {
                if (TextContentType.Text.equals(content.getType())) {
                    content.setContent(sensitiveWordBs.replace(content.getContent()));
                    sb.append(content.getContent());
                } else {
                    SysUserInfoVO userDto = JSONUtil.toBean(content.getContent(), SysUserInfoVO.class);
                    if (UserTypes.bot.equals(userDto.getType())) {
                        botUserRef.set(JSONUtil.toBean(content.getContent(), SysUserInfoVO.class));
                    }
                }
            });
            message.setMessage(JSONUtil.toJsonStr(contents));
        } else {
            message.setMessage(sendMessageDTO.getMsgContent());
        }
        message.setType(sendMessageDTO.getType());
        SysUserInfoVO user = sysUserService.getUserById(userId);
        message.setFromInfo(user);
        if (null == previousMessage) {
            message.setIsShowTime(true);
        } else {
            message.setIsShowTime(DateUtil.between
                    (new Date(), previousMessage.getUpdateTime(), DateUnit.MINUTE) > 5);
        }
        if (StrUtil.isNotBlank(sendMessageDTO.getReferenceMsgId())) {
            Message referenceMessage = getById(sendMessageDTO.getReferenceMsgId());
            referenceMessage.setReferenceMsg(null);
            message.setReferenceMsg(referenceMessage);
        }
        if (save(message)) {
            // @机器人回复
            SysUserInfoVO botUser = botUserRef.get();
            if (botUser != null) {
                aiChatService.sendBotReply(userId, sendMessageDTO.getTargetId(), botUser, sb.toString());
            }
            return message;
        }
        return null;
    }
}
