package com.mingri.service.notify.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.notify.repo.entity.Notify;
import org.apache.ibatis.annotations.Select;

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

}
