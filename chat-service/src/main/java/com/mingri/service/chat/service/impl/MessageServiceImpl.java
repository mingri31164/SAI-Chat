package com.mingri.service.chat.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mingri.model.constant.MessageConstant;
import com.mingri.model.constant.MessageSource;
import com.mingri.model.constant.type.MessageType;
import com.mingri.model.constant.type.TextContentType;
import com.mingri.model.context.BaseContext;
import com.mingri.model.vo.chat.message.dto.RecallDTO;
import com.mingri.model.vo.chat.message.dto.RecordDTO;
import com.mingri.model.vo.chat.message.dto.SendMessageDTO;
import com.mingri.model.vo.chat.message.dto.TextMessageContent;
import com.mingri.model.vo.sys.dto.SysUserInfoDTO;
import com.mingri.service.chat.repo.entity.MessageDO;
import com.mingri.model.enumeration.NotifyTypeEnum;
import com.mingri.model.enumeration.UserTypes;
import com.mingri.model.vo.notify.NotifyMsgEvent;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.service.AiChatService;
import com.mingri.service.chat.service.IChatListService;
import com.mingri.service.chat.service.IMessageService;
import com.mingri.service.chat.repo.mapper.MessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.core.utils.CacheUtil;
import com.mingri.service.user.service.ISysUserService;
import com.mingri.service.websocket.service.WebSocketService;
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
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessageDO> implements IMessageService {

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
    public MessageDO send(SendMessageDTO sendMessageDTO) {
        if (MessageSource.Group.equals(sendMessageDTO.getSource())) {
            return sendMessageToGroup(BaseContext.getCurrentId().toString(),sendMessageDTO);
        } else {
            return sendMessageToUser(sendMessageDTO);
        }
    }


    @Override
    //@DS("slave")
    public List<MessageDO> record(RecordDTO recordDTO) {
        String userId = BaseContext.getCurrentId();
        List<MessageDO> messages = messageMapper.record(userId, recordDTO.getTargetId(),
                recordDTO.getIndex(), recordDTO.getNum());
        cacheUtil.putUserReadCache(userId, recordDTO.getTargetId());
        return messages;
    }

    @Override
    public MessageDO recall(RecallDTO recallDTO) {
        String userId = BaseContext.getCurrentId();
        MessageDO message = getById(recallDTO.getMsgId());
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
        message.setType(MessageType.Retraction);
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
        LambdaQueryWrapper<MessageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(MessageDO::getCreateTime, expirationDate);
        if (remove(queryWrapper)) {
            log.info("---清理过期消息成功---");
        }
    }

    @Override
    public MessageDO sendMessageToGroup(String userId, SendMessageDTO sendMessageDTO) {
        MessageDO message = sendMessage(userId, sendMessageDTO , MessageSource.Group);
        NotifyMsgEvent<MessageDO> sendMessageToGroupEvent =
                new NotifyMsgEvent<>(this, NotifyTypeEnum.GROUP_CHAT, message);
        eventPublisher.publishEvent(sendMessageToGroupEvent);
        return message;
    }

    private MessageDO sendMessageToUser(SendMessageDTO sendMessageDTO) {
        String userId = BaseContext.getCurrentId();
        String targetId = sendMessageDTO.getTargetId();
        MessageDO message = sendMessage(userId, sendMessageDTO, MessageSource.User);
        chatListService.updateChatListPrivate(targetId, message);
        webSocketService.sendMsgToUser(message, userId, targetId);
        return message;
    }


    public MessageDO sendMessage(String userId, SendMessageDTO sendMessageDTO, String source) {
        //获取上一条显示时间的消息
        MessageDO previousMessage = messageMapper.getPreviousShowTimeMsg(userId, sendMessageDTO.getTargetId());
        //存入数据库
        MessageDO message = new MessageDO();
        message.setId(IdUtil.randomUUID());
        message.setFromId(userId);
        message.setSource(source);
        message.setToId(sendMessageDTO.getTargetId());
        StringBuffer sb = new StringBuffer();
        AtomicReference<SysUserInfoDTO> botUserRef = new AtomicReference<>(null);
        if (MessageType.Text.equals(sendMessageDTO.getType())) {
            // 敏感词替换
            List<TextMessageContent> contents = JSONUtil.toList(sendMessageDTO.getMsgContent(), TextMessageContent.class);
            contents.forEach(content -> {
                if (TextContentType.Text.equals(content.getType())) {
                    content.setContent(sensitiveWordBs.replace(content.getContent()));
                    sb.append(content.getContent());
                } else {
                    SysUserInfoDTO userDto = JSONUtil.toBean(content.getContent(), SysUserInfoDTO.class);
                    if (UserTypes.bot.equals(userDto.getType())) {
                        botUserRef.set(JSONUtil.toBean(content.getContent(), SysUserInfoDTO.class));
                    }
                }
            });
            message.setMessage(JSONUtil.toJsonStr(contents));
        } else {
            message.setMessage(sendMessageDTO.getMsgContent());
        }
        message.setType(sendMessageDTO.getType());
        SysUserInfoDTO user = sysUserService.getUserById(userId);
        message.setFromInfo(user);
        if (null == previousMessage) {
            message.setIsShowTime(true);
        } else {
            message.setIsShowTime(DateUtil.between
                    (new Date(), previousMessage.getUpdateTime(), DateUnit.MINUTE) > 5);
        }
        if (StrUtil.isNotBlank(sendMessageDTO.getReferenceMsgId())) {
            MessageDO referenceMessage = getById(sendMessageDTO.getReferenceMsgId());
            referenceMessage.setReferenceMsg(null);
            message.setReferenceMsg(referenceMessage);
        }
        if (save(message)) {
            // @机器人回复
            SysUserInfoDTO botUser = botUserRef.get();
            if (botUser != null) {
                aiChatService.sendBotReply(userId, sendMessageDTO.getTargetId(), botUser, sb.toString());
            }
            return message;
        }
        return null;
    }
}
