package com.mingri.service.notify.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.chat.repo.vo.friend.dto.FriendNotifyDto;
import com.mingri.service.notify.repo.vo.dto.SystemNotifyDto;
import com.mingri.service.notify.repo.vo.entity.Notify;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知 Mapper 接口
 */
public interface NotifyMapper extends BaseMapper<Notify> {

    @Select("SELECT COUNT(*) FROM `notify`" +
            "WHERE (`to_id` = #{userId} OR `from_id` = #{userId}) AND `unread_id` = #{userId}")
    Integer unreadByUserId(String userId);

    @Select("SELECT COUNT(*) FROM `notify` " +
            "WHERE (`to_id` = #{userId} OR `from_id` = #{userId}) " +
            "AND `unread_id` = #{userId} " +
            "AND `type` = #{type} ")
    Integer unreadByType(String userId, String type);

    @Select("select n.*, u.`name` as `from_name`, u.`portrait` as `from_portrait`, " +
            "u2.`name` AS `to_name`,u2.`portrait` AS `to_portrait` " +
            "from `notify` as n " +
            "         left join `user` as u on n.`from_id` = u.`id` " +
            "         left join `user` as u2 on n.`to_id` = u2.`id` " +
            "where n.`type` = #{type} " +
            "  and (n.`from_id` = #{userId} or n.`to_id` = #{userId}) " +
            "ORDER BY `create_time` DESC")
    List<FriendNotifyDto> friendListNotify(String userId, String type);

    @Select("SELECT * FROM `notify`" +
            "WHERE `type` = 'system' " +
            "ORDER BY `create_time` DESC")
    @ResultMap("SystemNotifyDtoResultMap")
    List<SystemNotifyDto> SystemListNotify();


}
