package com.mingri.service.chat.repo.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.chat.repo.dto.Top10MsgDto;
import com.mingri.service.chat.repo.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 消息表 Mapper 接口
 */
public interface MessageMapper extends BaseMapper<Message> {
    @Select("SELECT * " +
            "FROM (SELECT * " +
            "      FROM `message` " +
            "      WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "         OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "         OR (`source` = 'group' AND `to_id` = #{targetId}) " +
            "      ORDER BY `create_time` DESC LIMIT #{index}, #{num}) AS subquery " +
            "ORDER BY `create_time` ASC")
    @ResultMap("mybatis-plus_Message")
    List<Message> messageRecord(@Param("userId") String userId, @Param("targetId") String targetId, @Param("index") int index, @Param("num") int num);

    @Select("SELECT * " +
            "FROM (SELECT * " +
            "      FROM `message` " +
            "      WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "         OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "         OR (`source` = 'group' AND `to_id` = #{targetId}) " +
            "      ORDER BY `create_time` DESC LIMIT #{index}, #{num}) AS subquery ")
    @ResultMap("mybatis-plus_Message")
    List<Message> messageRecordDesc(@Param("userId") String userId, @Param("targetId") String targetId, @Param("index") int index, @Param("num") int num);


    @Select("SELECT * " +
            "FROM `message` " +
            "WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "   OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "ORDER BY `create_time` DESC LIMIT 1")
    @ResultMap("mybatis-plus_Message")
    Message getPreviousShowTimeMsg(@Param("userId") String userId, @Param("targetId") String toUserId);

    @Select("select count(*) from `message` where create_time >= #{date} " +
            "    AND create_time < DATE_ADD(#{date}, INTERVAL 1 DAY) ")
    Integer messageNum(DateTime date);

    @Select("SELECT u.id, u.account, u.name, u.portrait, COUNT(m.id) AS num " +
            "FROM message m " +
            "JOIN user u ON m.from_id = u.id " +
            "WHERE m.create_time >= DATE(#{date}) " +
            "  AND m.create_time < DATE_ADD(DATE (#{date}), INTERVAL 1 DAY) " +
            "GROUP BY u.id " +
            "ORDER BY num DESC " +
            "LIMIT 10 ")
    List<Top10MsgDto> getTop10Msg(Date date);
}