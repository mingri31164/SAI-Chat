package com.mingri.service.chat.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.chat.repo.entity.ChatList;
import org.apache.ibatis.annotations.Select;


/**
 * 聊天列表 Mapper 接口
 */
public interface ChatListMapper extends BaseMapper<ChatList> {


    @Select("SELECT SUM(`unread_num`) FROM `chat_list` " +
            "WHERE `user_id` = #{userId}")
    Integer unreadByUserId(String userId);
}
