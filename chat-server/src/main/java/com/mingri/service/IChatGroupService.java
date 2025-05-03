package com.mingri.service;

import com.mingri.entity.ChatGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.vo.chatGroup.CreateChatGroupVo;
import com.mingri.vo.chatGroup.DissolveChatGroupVo;
import com.mingri.vo.chatGroup.UpdateChatGroupVo;

import java.util.List;

/**
 * <p>
 * 聊天群表 服务类
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
public interface IChatGroupService extends IService<ChatGroup> {

    /**
     * 获取群列表
     */
    List<ChatGroup> chatGroupList(String userId);

    /**
     * 创建群聊
     **/
    boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo);

    /**
     * 更新群聊信息
     **/
    boolean updateChatGroup(String userId, UpdateChatGroupVo updateChatGroupVo);


}
