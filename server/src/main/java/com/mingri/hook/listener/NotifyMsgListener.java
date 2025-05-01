package com.mingri.hook.listener;

import com.mingri.entity.Message;
import com.mingri.event.NotifyMsgEvent;
import com.mingri.service.IChatListService;
import com.mingri.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Description: 消息事件监听器
 * @Author: mingri31164
 * @Date: 2025/2/6 22:30
 **/
@Component
@Slf4j
public class NotifyMsgListener<T> implements ApplicationListener<NotifyMsgEvent<T>> {
    @Autowired
    private IChatListService chatListService;
    @Autowired
    private WebSocketService webSocketService;


    @Async("notify")
    @SuppressWarnings("unchecked")
    @Override
    public void onApplicationEvent(NotifyMsgEvent<T> msgEvent) {
        switch (msgEvent.getNotifyType()) {
            case GROUP_CHAT:
                updateChatListGroup((NotifyMsgEvent<Message>) msgEvent);
                break;
            default:
                // todo 系统消息
        }
    }

    private void updateChatListGroup(NotifyMsgEvent<Message> msgEvent) {
        Message message = msgEvent.getContent();
        chatListService.updateChatListGroup(message);
        webSocketService.sendMsgToGroup(message);
    }


}
