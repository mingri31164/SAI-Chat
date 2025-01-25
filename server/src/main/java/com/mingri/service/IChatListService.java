package com.mingri.service;

import com.mingri.entity.ChatList;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.entity.Message;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-24
 */
public interface IChatListService extends IService<ChatList> {

    /**
     * @Description: 获取私聊列表
     * @Author: mingri31164
     * @Date: 2025/1/25 17:05
     **/
    List<ChatList> privateList();

    /**
     * @Description: 获取群聊列表
     * @Author: mingri31164
     * @Date: 2025/1/25 17:05
     **/
    ChatList getGroup();

    /**
     * @Description: 添加私聊对象
     * @Author: mingri31164
     * @Date: 2025/1/25 17:05
     **/
    ChatList create(String targetId);

    boolean read(String targetId);

    boolean delete(String chatListId);

    boolean updateChatListPrivate(String targetId, Message message);

    boolean updateChatListGroup(Message message);
}
