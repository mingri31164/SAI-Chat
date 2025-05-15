package com.mingri.service.chat.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.mingri.model.constant.MessageSource;
import com.mingri.model.constant.type.MessageType;
import com.mingri.model.constant.type.TextContentType;
import com.mingri.model.vo.chat.message.dto.SendMessageDTO;
import com.mingri.model.vo.chat.message.dto.TextMessageContent;
import com.mingri.model.vo.sys.dto.SysUserInfoDTO;
import com.mingri.service.user.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class AiChatService {

    @Resource
    @Lazy
    ISysUserService userService;

    @Resource
    @Lazy
    IMessageService messageService;

    @Resource
    DoubaoAiService doubaoAiService;

    @Async("douBao")
    public void sendBotReply(String userId, String targetId, SysUserInfoDTO botUser, String content) {
        SysUserInfoDTO user = userService.getUserById(userId);
        // 创建消息
        // at内容
        TextMessageContent atUser = new TextMessageContent();
        atUser.setType(TextContentType.At);
        JSONConfig config = new JSONConfig().setIgnoreNullValue(true);
        atUser.setContent(JSONUtil.toJsonStr(user, config));
        // 文本消息内容
        String ask = doubaoAiService.ask(userId, content);
        TextMessageContent msgText = new TextMessageContent();
        msgText.setType(TextContentType.Text);
        msgText.setContent(ask);
        // 合并消息内容
        JSONArray msgContent = new JSONArray();
        msgContent.add(atUser);
        msgContent.add(msgText);
        // 发送消息
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setTargetId(targetId);
        sendMessageDTO.setSource(MessageSource.Group);
        sendMessageDTO.setMsgContent(msgContent.toJSONString(0));
        sendMessageDTO.setType(MessageType.Text);
        messageService.sendMessageToGroup(botUser.getId(), sendMessageDTO);
    }
}
