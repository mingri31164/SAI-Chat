package com.mingri.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mingri.constant.JwtClaimsConstant;
import com.mingri.constant.MessageConstant;
import com.mingri.constant.type.WsContentType;
import com.mingri.dto.message.NotifyDto;
import com.mingri.entity.Message;
import com.mingri.properties.JwtProperties;
import com.mingri.result.Result;
import com.mingri.utils.CacheUtil;
import com.mingri.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;


@Slf4j
@Service
public class WebSocketService {

    @Data
    public static class WsContent {
        private String type;
        private Object content;
    }

    @Resource
    @Lazy
    ISysUserService userService;

    @Resource
    CacheUtil cacheUtil;
    @Autowired
    private JwtProperties jwtProperties;

    public static final ConcurrentHashMap<String, Channel> Online_User = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Channel, String> Online_Channel = new ConcurrentHashMap<>();

    public void online(Channel channel, String token) {
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(),token);
            String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
            String cacheToken = cacheUtil.getUserSessionCache(userId);
            if (!token.equals(cacheToken)) {
                sendMsg(channel, Result.error(MessageConstant.LOGIN_IN_OTHER_PLACE), WsContentType.Msg);
                channel.close();
                return;
            }
            Online_User.put(userId, channel);
            Online_Channel.put(channel, userId);
            userService.online(userId);
        } catch (Exception e) {
            sendMsg(channel, Result.error(MessageConstant.CONNECT_ERROR), WsContentType.Msg);
            channel.close();
        }
    }

    public void offline(Channel channel) {
        String userId = Online_Channel.get(channel);
        if (StrUtil.isNotBlank(userId)) {
            Online_User.remove(userId);
            Online_Channel.remove(channel);
            userService.offline(userId);
        }
    }

    private void sendMsg(Channel channel, Object msg, String type) {
        WsContent wsContent = new WsContent();
        wsContent.setType(type);
        wsContent.setContent(msg);
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(wsContent)));
    }

    public void sendMsgToUser(Object msg, String userId, String targetId) {
        Channel channel = Online_User.get(userId);
        if (channel != null) {
            sendMsg(channel, msg, WsContentType.Msg);
        }
        channel = Online_User.get(targetId);
        if (channel != null) {
            sendMsg(channel, msg, WsContentType.Msg);
        }
    }

    public void sendMsgToGroup(Message message) {
        Online_Channel.forEach((channel, ext) -> {
            sendMsg(channel, message, WsContentType.Msg);
        });
    }

    public Integer getOnlineNum() {
        return Online_User.size();
    }

    public List<String> getOnlineUser() {
        return new ArrayList<>(Online_User.keySet());
    }

    public void sendNotifyToGroup(NotifyDto notify) {
        Online_Channel.forEach((channel, ext) -> {
            sendMsg(channel, notify, WsContentType.Notify);
        });
    }


    public void sendVideoToUser(Object msg, String userId) {
        Channel channel = Online_User.get(userId);
        if (channel != null) {
            sendMsg(channel, msg, WsContentType.Video);
        }
    }

    public void sendFileToUser(Object msg, String userId) {
        Channel channel = Online_User.get(userId);
        if (channel != null) {
            sendMsg(channel, msg, WsContentType.File);
        }
    }
}
