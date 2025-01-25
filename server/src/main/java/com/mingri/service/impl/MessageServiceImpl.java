package com.mingri.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mingri.constant.*;
import com.mingri.context.BaseContext;
import com.mingri.dto.message.RecallDTO;
import com.mingri.dto.message.RecordDTO;
import com.mingri.dto.message.SendMessageDTO;
import com.mingri.dto.message.TextMessageContent;
import com.mingri.entity.Message;
import com.mingri.entity.SysUser;
import com.mingri.enumeration.UserTypes;
import com.mingri.exception.BaseException;
import com.mingri.mapper.MessageMapper;
import com.mingri.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.utils.CacheUtil;
import com.mingri.vo.SysUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-24
 */

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

    @Override
    public Message send(SendMessageDTO sendMessageDTO) {
        SysUser tarUser = Db.lambdaQuery(SysUser.class).eq(SysUser::getId, sendMessageDTO.getTargetId()).one();
        if (null == tarUser){
            throw new BaseException(MessageConstant.USER_NOT_EXIST);
        }

        if (MessageSource.Group.equals(sendMessageDTO.getSource())) {
            return sendMessageToGroup(sendMessageDTO);
        } else {
            return sendMessageToUser(sendMessageDTO);
        }    }

    @Override
    public List<Message> record(RecordDTO recordDTO) {
        String userId = String.valueOf(BaseContext.getCurrentId());
        List<Message> messages = messageMapper.record(userId, recordDTO.getTargetId(),
                recordDTO.getIndex(), recordDTO.getNum());
        cacheUtil.putUserReadCache(userId, recordDTO.getTargetId());
        return messages;
    }

    @Override
    public Message recall(RecallDTO recallDTO) {
        String userId = String.valueOf(BaseContext.getCurrentId());
        Message message = getById(recallDTO.getMsgId());
        if (null == message) {
            throw new BaseException(MessageConstant.MESSAGE_NOT_EXIST);
        }
        if (!message.getFromId().equals(userId)) {
            throw new BaseException(MessageConstant.ERROR_ONLY_SELF_RECALL);
        }

        Date createTime = message.getCreateTime();
        log.info("消息创建时间:{}", createTime);
        log.info("当前时间:{}", new Date());
        log.info("距离消息发出已经有:{}", DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE));

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
    public Message sendMessageToGroup(String userId, SendMessageDTO sendMessageDTO) {
        Message message = sendMessage(String.valueOf(userId), sendMessageDTO , MessageSource.Group);
        //更新群聊列表
        chatListService.updateChatListGroup(message);
        webSocketService.sendMsgToGroup(message);
        return message;
    }

    private Message sendMessageToUser(SendMessageDTO sendMessageDTO) {
        String userId = String.valueOf(BaseContext.getCurrentId());
        Message message = sendMessage(userId, sendMessageDTO, MessageSource.User);
        //更新私聊列表
        chatListService.updateChatListPrivate(sendMessageDTO.getTargetId(), message);
        webSocketService.sendMsgToUser(message, String.valueOf(userId), sendMessageDTO.getTargetId());
        return message;
    }

    private Message sendMessageToGroup(SendMessageDTO sendMessageDTO) {
        String userId = String.valueOf(BaseContext.getCurrentId());
        Message message = sendMessage(userId, sendMessageDTO, MessageSource.Group);
        //更新群聊列表
        chatListService.updateChatListGroup(message);
        webSocketService.sendMsgToGroup(message);
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
                    if (UserTypes.BOT.equals(userDto.getUserType())) {
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
            message.setIsShowTime(DateUtil.between(new Date(), previousMessage.getUpdateTime(), DateUnit.MINUTE) > 5);
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
