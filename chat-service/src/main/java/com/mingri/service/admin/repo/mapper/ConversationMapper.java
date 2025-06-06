package com.mingri.service.admin.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.admin.repo.vo.entity.Conversation;
import com.mingri.service.admin.repo.vo.dto.ConversationDto;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("SELECT c.*, u.`name`, u.`portrait`, u.`account` " +
            "FROM `conversation` AS c " +
            "JOIN `user` AS u ON u.`id` = c.`user_id` ")
    List<ConversationDto> conversationList();
}
