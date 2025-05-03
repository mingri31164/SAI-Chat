package com.mingri.mapper;

import com.mingri.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import java.util.List;



@Mapper
public interface MessageMapper extends BaseMapper<Message> {


    @Select("SELECT * " +
            "      FROM `message` " +
            "      WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "         OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "         OR (`source` = 'group' AND `to_id` = #{targetId}) " +
            "      ORDER BY `create_time` DESC LIMIT #{index}, #{num} ")
    @ResultMap("RecordVoMap")
    List<Message> record(String userId, String targetId, int index, int num);

    @Select("SELECT * " +
            "FROM `message` " +
            "WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "   OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "ORDER BY `create_time` DESC LIMIT 1")
    Message getPreviousShowTimeMsg(String userId, String targetId);}
