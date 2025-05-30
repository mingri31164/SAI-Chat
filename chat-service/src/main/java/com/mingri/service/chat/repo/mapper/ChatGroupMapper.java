package com.mingri.service.chat.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.chat.repo.vo.chatgroup.dto.ChatGroupDetailsDto;
import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroup;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天群表 Mapper 接口
 */
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    @Select("SELECT cg.*, cgm.`group_remark` AS `group_remark` FROM `chat_group` AS cg " +
            "LEFT JOIN `chat_group_member` AS cgm ON cg.`id` = cgm.`chat_group_id` AND cgm.`user_id` = #{userId} " +
            "WHERE cgm.`user_id` = #{userId}")
    List<ChatGroup> getList(String userId);

    @Select("SELECT cg.*, cgm.`group_remark` AS `group_remark` FROM `chat_group` AS cg " +
            "LEFT JOIN `chat_group_member` AS cgm ON cg.`id` = cgm.`chat_group_id` AND cgm.`user_id` = #{userId} " +
            "WHERE cgm.`user_id` = #{userId} AND (cg.`name` LIKE CONCAT('%', #{search}, '%') " +
            "OR cg.`chat_group_number` LIKE CONCAT('%', #{search}, '%')" +
            "OR cgm.group_remark LIKE CONCAT('%', #{search}, '%'))")
    List<ChatGroup> getListFromSearch(String userId, String search);

    @Select("SELECT cg.*,cgm.`group_name`,cgm.`group_remark` from `chat_group` as cg " +
            "LEFT JOIN `chat_group_member` as cgm on cg.`id` = cgm.`chat_group_id` " +
            "where cg.`id` = #{chatGroupId} AND cgm.`user_id`=#{userId} ")
    @ResultMap("ChatGroupDetailsDtoResultMap")
    ChatGroupDetailsDto detailsChatGroup(String userId, String chatGroupId);
}
