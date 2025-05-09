package com.mingri.mapper;

import com.mingri.entity.chat.ChatGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 聊天群表 Mapper 接口
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {
    @Select("SELECT cg.*, cgm.`group_remark` AS `group_remark` FROM `chat_group` AS cg " +
            "LEFT JOIN `chat_group_member` AS cgm ON cg.`id` = cgm.`chat_group_id` AND cgm.`user_id` = #{userId} " +
            "WHERE cgm.`user_id` = #{userId}")
    List<ChatGroup> getList(String userId);
}
